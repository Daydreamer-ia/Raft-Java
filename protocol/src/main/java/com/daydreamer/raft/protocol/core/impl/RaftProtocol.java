package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.request.EntryCommittedRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.api.entity.response.EntryCommittedResponse;
import com.daydreamer.raft.api.entity.response.ServerErrorResponse;
import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import com.daydreamer.raft.protocol.storage.impl.DelegateReplicatedStateMachine;
import com.daydreamer.raft.protocol.storage.impl.MemoryReplicatedStateMachine;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 * <p>
 * raft protocol
 */
public class RaftProtocol implements Protocol {
    
    private static final Logger LOGGER = Logger.getLogger(RaftProtocol.class);
    
    private AbstractRaftServer raftServer;
    
    private RaftConfig raftConfig;
    
    private RaftMemberManager raftMemberManager;
    
    private ReplicatedStateMachine replicatedStateMachine;
    
    public RaftProtocol(String raftConfigPath) {
        // init reader, avoid gc
        PropertiesReader<RaftConfig> raftConfigPropertiesReader = new RaftPropertiesReader(raftConfigPath);
        raftMemberManager = new MemberManager(raftConfigPropertiesReader.getProperties());
        replicatedStateMachine = new DelegateReplicatedStateMachine(raftMemberManager, new MemoryReplicatedStateMachine());
        raftConfig = raftConfigPropertiesReader.getProperties();
        // init server
        this.raftServer = new GrpcRaftServer(raftConfigPropertiesReader, raftMemberManager,
                new GrpcFollowerNotifier(raftMemberManager, raftConfigPropertiesReader.getProperties()),
                replicatedStateMachine);
    }
    
    @Override
    public boolean write(Payload payload) throws Exception {
        // if abnormal
        if (!raftServer.normalCluster()) {
            throw new IllegalStateException(
                    "[RaftProtocol] - The current cluster status is abnormal, may be no leader found!");
        }
        // no leader
        if (!raftServer.isLeader()) {
            throw new IllegalStateException("[RaftProtocol] - Current node is not leader!");
        }
        // normal status, success if append half of cluster nodes successfully
        List<Member> allMember = raftMemberManager.getAllMember();
        Member self = raftMemberManager.getSelf();
        LogEntry logEntry = new LogEntry(self.getTerm(), self.getLogId() + 1, payload);
        replicatedStateMachine.append(logEntry);
        // increase log id
        self.increaseLogId();
        // try to append one
        int successCount = 0;
        List<Member> finish = new ArrayList<>();
        for (Member member : allMember) {
            int retryTimes = raftConfig.getWriteRetryTimes();
            while (retryTimes >= 0) {
                try {
                    // append
                    if (doAppendLog(member, logEntry)) {
                        // finish append
                        finish.add(member);
                        successCount++;
                        break;
                    } else {
                        retryTimes--;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // fail to append
                    retryTimes--;
                }
            }
        }
        // success
        if (successCount + 1 > (allMember.size() + 1) / 2) {
            finish.forEach(member -> {
                member.setLogId(logEntry.getLogId());
                member.setTerm(logEntry.getTerm());
            });
            EntryCommittedRequest committed = new EntryCommittedRequest(logEntry.getLogId(), logEntry.getTerm());
            if (commit(committed, finish)) {
                replicatedStateMachine.commit(logEntry.getTerm(), logEntry.getLogId());
                return true;
            }
        }
        return false;
    }
    
    /**
     * committed
     *
     * @param request request
     * @param members members
     */
    private boolean commit(EntryCommittedRequest request, List<Member> members) {
        CountDownLatch countDownLatch = new CountDownLatch(members.size());
        AtomicInteger count = new AtomicInteger(0);
        try {
            members.forEach(member -> {
                try {
                    Connection connection = member.getConnection();
                    if (connection != null) {
                        connection.request(request, new ResponseCallBack() {
                            @Override
                            public void onSuccess(Response response) {
                                if (response instanceof EntryCommittedResponse) {
                                    if (((EntryCommittedResponse) response).isAccepted()) {
                                        count.incrementAndGet();
                                    }
                                }
                                countDownLatch.countDown();
                            }
                            
                            @Override
                            public void onFail(Exception e) {
                                countDownLatch.countDown();
                            }
                            
                            @Override
                            public void onTimeout() {
                                countDownLatch.countDown();
                            }
                        });
                    }
                } catch (Exception e) {
                    // nothing to do
                }
            });
            countDownLatch.await(2500 * members.size(), TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            // nothing to do
        }
        return count.get() + 1 > (members.size() + 1) / 2;
    }
    
    /**
     * request to member
     *
     * @param member    member
     * @param newestLog newest Log
     */
    private boolean doAppendLog(Member member, LogEntry newestLog) throws Exception {
        // append log request
        AppendEntriesRequest originRequest = buildAppendLogRequest(Collections.singletonList(newestLog));
        Connection connection = member.getConnection();
        if (connection != null) {
            AppendEntriesResponse response = new AppendEntriesResponse(false);
            // need to syn log
            while (!response.isAccepted()) {
                member.setLogId(originRequest.getLastLogId() - 1);
                Future<Response> future = connection.request(originRequest);
                // block wait
                Response commonResponse = future.get();
                // member may down
                // copy log next time
                if (commonResponse == null) {
                    return false;
                }
                // if submit log has committed
                if (commonResponse instanceof ServerErrorResponse) {
                    LOGGER.error("Node has committed the log, member: " + member.getAddress());
                    return false;
                } else if (commonResponse instanceof AppendEntriesResponse) {
                    response = (AppendEntriesResponse) commonResponse;
                }
                // find last log
                if (originRequest.getLastLogId() - 1 < 0) {
                    originRequest.setLastTerm(0);
                    originRequest.setLastLogId(-1);
                    continue;
                }
                LogEntry lastLog = replicatedStateMachine.getLogById(originRequest.getLastLogId() - 1);
                int lastTerm = 0;
                long lastLogId = -1;
                if (originRequest.getLastLogId() - 2 >= 0) {
                    LogEntry lastLastLog = replicatedStateMachine.getLogById(originRequest.getLastLogId() - 2);
                    lastLogId = lastLastLog.getLogId();
                    lastTerm = lastLastLog.getTerm();
                }
                originRequest.setLastTerm(lastTerm);
                originRequest.setLastLogId(lastLogId);
                originRequest.getLogEntries().add(0, lastLog);
            }
            return true;
        }
        return false;
    }
    
    /**
     * build request
     *
     * @param logEntries log entries
     */
    private AppendEntriesRequest buildAppendLogRequest(List<LogEntry> logEntries) {
        Member self = raftMemberManager.getSelf();
        AppendEntriesRequest appendEntriesRequest = new AppendEntriesRequest();
        appendEntriesRequest.setCurrentLogId(self.getLogId());
        appendEntriesRequest.setCurrentTerm(self.getTerm());
        appendEntriesRequest.setLogEntries(logEntries);
        appendEntriesRequest.setPayload(true);
        long lastLogId = logEntries.get(0).getLogId() - 1;
        int lastTerm = 0;
        if (lastLogId >= 0) {
            LogEntry lastLog = replicatedStateMachine.getLogById(lastLogId);
            lastTerm = lastLog.getTerm();
        }
        appendEntriesRequest.setLastLogId(lastLogId);
        appendEntriesRequest.setLastTerm(lastTerm);
        return appendEntriesRequest;
    }
    
    @Override
    public void read() {
        throw new UnsupportedOperationException("Current version don't support this action.");
    }
    
    @Override
    public void run() {
        raftServer.start();
    }
    
    @Override
    public void close() {
        raftServer.close();
        LOGGER.info("Close finish!");
    }
}
