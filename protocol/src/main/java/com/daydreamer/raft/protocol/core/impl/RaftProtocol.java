package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.base.CommittedResponse;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.MemberChangeEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.constant.LogType;
import com.daydreamer.raft.api.entity.request.EntryCommittedRequest;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.common.loader.ServiceFactory;
import com.daydreamer.raft.common.loader.impl.ConfigServiceFactory;
import com.daydreamer.raft.common.service.ActiveProperties;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.LogSender;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 * <p>
 * raft protocol
 */
public class RaftProtocol implements Protocol {

    private static final Logger LOGGER = Logger.getLogger(RaftProtocol.class);

    private static final String MEMBER_CHANGE_KEY = "memberChange";

    private static final String ADDRESS_KEY = "address";

    private static final String CONFIG_FACTORY_KEY = "configServiceFactory";

    private static final String CONFIG_NAME = "raftConfig";

    private AbstractRaftServer raftServer;

    private RaftPropertiesReader raftConfigPropertiesReader;

    private RaftConfig raftConfig;

    private RaftMemberManager raftMemberManager;

    private ReplicatedStateMachine replicatedStateMachine;

    private LogSender logSender;

    private String groupKey;

    public RaftProtocol(String raftConfigPath) {
        init(raftConfigPath, null);
    }

    public RaftProtocol(RaftConfig raftConfig) {
        init(null, raftConfig);
    }

    /**
     * init
     */
    private void init(String raftConfigPath, RaftConfig raftConfig) {
        groupKey = UUID.randomUUID().toString();
        initConfig(raftConfigPath, raftConfig);
        raftMemberManager = RaftServiceLoader.getLoader(groupKey, RaftMemberManager.class).getDefault();
        replicatedStateMachine = RaftServiceLoader.getLoader(groupKey, ReplicatedStateMachine.class).getDefault();
        this.raftConfig = raftConfigPropertiesReader.getProperties();
        logSender = RaftServiceLoader.getLoader(groupKey, LogSender.class).getDefault();
        // init server
        this.raftServer = RaftServiceLoader.getLoader(groupKey, AbstractRaftServer.class).getDefault();
    }

    private void initConfig(String raftConfigPath, RaftConfig raftConfig) {
        // init reader, avoid gc
        if (raftConfig != null) {
            this.raftConfigPropertiesReader = new RaftPropertiesReader(raftConfig);
        } else {
            this.raftConfigPropertiesReader = new RaftPropertiesReader(raftConfigPath);
        }
        // inject to loader
        ConfigServiceFactory configServiceFactory = (ConfigServiceFactory) RaftServiceLoader
                .getLoader(groupKey, ServiceFactory.class)
                .getInstance(CONFIG_FACTORY_KEY);
        raftConfig = this.raftConfigPropertiesReader.getProperties();
        configServiceFactory.addProperty(CONFIG_NAME, raftConfig);
    }

    @Override
    public synchronized boolean write(Payload payload) throws Exception {
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
    public boolean memberChange(MemberChangeEntry memberChangeEntry) throws Exception {
        if (memberChangeEntry == null || memberChangeEntry.getAddress() == null) {
            throw new IllegalArgumentException("payload is null!");
        }
        if (memberChangeEntry.getMemberChange() == null) {
            throw new IllegalArgumentException("Illegal request for member changing!");
        }
        // try to write
        Payload payload = new Payload();
        Map<String, String> map = new HashMap<>(2);
        map.put(MEMBER_CHANGE_KEY, memberChangeEntry.getMemberChange().toString());
        map.put(ADDRESS_KEY, memberChangeEntry.getAddress());
        payload.setLogType(LogType.MEMBER_CHANGE);
        payload.setMetadata(map);
        // write
        return write(payload);
    }

    @Override
    public void run() {
        raftServer.start();
    }

    @Override
    public void close() {
        // if leave, it will be close by self
        if (!raftMemberManager.isSelfLeave()) {
            raftServer.close();
            raftConfigPropertiesReader.close();
        }
    }

    public void setRaftServer(AbstractRaftServer raftServer) {
        this.raftServer = raftServer;
    }
}
