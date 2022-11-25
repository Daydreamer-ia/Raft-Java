package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.base.CommittedResponse;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.MemberChangeEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.constant.LogType;
import com.daydreamer.raft.api.entity.request.EntryCommittedRequest;
import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.chain.LogPostProcessorHolder;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.LogSender;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import com.daydreamer.raft.protocol.storage.impl.DelegateReplicatedStateMachine;
import com.daydreamer.raft.protocol.storage.impl.MemoryReplicatedStateMachine;

import java.util.ArrayList;
import java.util.List;

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
    
    private LogSender logSender;
    
    public RaftProtocol(String raftConfigPath) {
        // init reader, avoid gc
        PropertiesReader<RaftConfig> raftConfigPropertiesReader = new RaftPropertiesReader(raftConfigPath);
        raftMemberManager = new MemberManager(raftConfigPropertiesReader);
        replicatedStateMachine = new DelegateReplicatedStateMachine(raftMemberManager,
                new MemoryReplicatedStateMachine(), new LogPostProcessorHolder());
        raftConfig = raftConfigPropertiesReader.getProperties();
        logSender = new DefaultLogSender(raftMemberManager, replicatedStateMachine);
        // init server
        this.raftServer = new GrpcRaftServer(raftConfigPropertiesReader, raftMemberManager,
                new GrpcFollowerNotifier(raftMemberManager, raftConfigPropertiesReader.getProperties()),
                replicatedStateMachine, logSender);
    }
    
    @Override
    public synchronized boolean write(Payload<?> payload) throws Exception {
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
                    if (logSender.appendLog(member, logEntry)) {
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
            // reset log index
            finish.forEach(member -> {
                member.setLogId(logEntry.getLogId());
                member.setTerm(logEntry.getTerm());
            });
            EntryCommittedRequest committed = new EntryCommittedRequest(logEntry.getLogId(), logEntry.getTerm());
            if (logSender.batchRequestMembers(committed, finish, (response) -> {
                if (response instanceof CommittedResponse) {
                    return ((CommittedResponse) response).isAccepted();
                }
                return false;
            })) {
                replicatedStateMachine.commit(logEntry.getTerm(), logEntry.getLogId());
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    public void read() {
        throw new UnsupportedOperationException("Current version don't support this action.");
    }
    
    @Override
    public void memberChange(Payload<MemberChangeEntry> payload) throws Exception {
        if (payload == null || payload.getObject() == null) {
            throw new IllegalArgumentException("payload is null!");
        }
        if (!LogType.MEMBER_CHANGE.equals(payload.getLogType())) {
            throw new IllegalArgumentException("Illegal request for member changing!");
        }
        if (payload.getObject().getMemberChange() == null) {
            throw new IllegalArgumentException("Illegal request for member changing!");
        }
        // if has send
        // TODO finish member change follow
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
