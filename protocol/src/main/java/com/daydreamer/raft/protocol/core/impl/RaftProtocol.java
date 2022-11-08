package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.storage.StorageRepository;
import com.daydreamer.raft.protocol.storage.impl.DelegateStorageRepository;
import com.daydreamer.raft.protocol.storage.impl.MemoryLogRepository;
import com.daydreamer.raft.transport.connection.Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Daydreamer
 * <p>
 * raft protocol
 */
public class RaftProtocol implements Protocol {
    
    private static final Logger LOGGER = Logger.getLogger(RaftProtocol.class.getSimpleName());
    
    private AbstractRaftServer raftServer;
    
    private PropertiesReader<RaftConfig> raftConfigPropertiesReader;
    
    private RaftMemberManager raftMemberManager;
    
    private StorageRepository storageRepository;
    
    public RaftProtocol(String raftConfigPath) {
        // init reader, avoid gc
        raftConfigPropertiesReader = new RaftPropertiesReader(raftConfigPath);
        raftMemberManager = new MemberManager(raftConfigPropertiesReader.getProperties());
        storageRepository = new DelegateStorageRepository(raftMemberManager, new MemoryLogRepository());
        // init server
        this.raftServer = new GrpcRaftServer(raftConfigPropertiesReader.getProperties(), raftMemberManager,
                new GrpcFollowerNotifier(raftMemberManager, raftConfigPropertiesReader.getProperties()),
                storageRepository);
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
        // try to append one
        AppendEntriesRequest originRequest = buildAppendLogRequest(Collections.singletonList(logEntry));
        int successCount = 0;
        List<Member> finish = new ArrayList<>();
        for (Member member : allMember) {
            int retryTimes = raftConfigPropertiesReader.getProperties().getWriteRetryTimes();
            while (retryTimes >= 0) {
                try {
                    // append
                    if (doAppendLog(member, originRequest)) {
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
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * request to member
     *
     * @param member        member
     * @param originRequest request
     */
    private boolean doAppendLog(Member member, AppendEntriesRequest originRequest) throws Exception {
        Connection connection = member.getConnection();
        if (connection != null) {
            AppendEntriesResponse response = new AppendEntriesResponse(false);
            // need to syn log
            while (!response.isAccepted()) {
                member.setLogId(originRequest.getLastLogId() - 1);
                response = (AppendEntriesResponse) connection.request(originRequest, 3000);
                if (response == null) {
                    return false;
                }
                // find last log
                LogEntry lastLog = storageRepository.getLogById(originRequest.getLastLogId() - 1);
                originRequest.setLastTerm(lastLog.getTerm());
                originRequest.setLastLogId(lastLog.getLogId());
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
        // batch try
        AppendEntriesRequest appendEntriesRequest = new AppendEntriesRequest();
        appendEntriesRequest.setCurrentLogId(self.getLogId());
        appendEntriesRequest.setCurrentTerm(self.getTerm());
        appendEntriesRequest.setLogEntries(logEntries);
        appendEntriesRequest.setPayload(true);
        LogEntry lastLog = storageRepository.getLogById(logEntries.get(0).getLogId() - 1);
        appendEntriesRequest.setLastLogId(lastLog.getLogId());
        appendEntriesRequest.setLastTerm(lastLog.getTerm());
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
}
