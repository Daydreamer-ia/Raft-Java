package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.constant.LogType;
import com.daydreamer.raft.common.annotation.SPI;
import com.daydreamer.raft.common.constant.LogConstant;
import com.daydreamer.raft.common.utils.MsgUtils;
import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.common.entity.RaftConfig;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Daydreamer
 */
@SPI("abstractRaftServer")
public abstract class AbstractRaftServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRaftServer.class.getSimpleName());
    
    /**
     * no-op
     */
    private static final Map<String, String> NO_OP_META_DATA = new HashMap<>();

    static {
        // inner log
        NO_OP_META_DATA.put(LogConstant.INNER_LOG_TAG, Boolean.TRUE.toString());
    }

    /**
     * if there is a leader in cluster
     */
    protected AtomicBoolean normalCluster = new AtomicBoolean(false);
    
    /**
     * last time when leader active
     * <p>
     * update if current node is follower and receive leader heartbeat
     */
    protected volatile long leaderLastActiveTime;
    
    /**
     * to be candidate start time
     * <p>
     * update if current node is follower and receive other node vote request
     */
    protected volatile long beCandidateStartTime;
    
    /**
     * last term current node has voted
     */
    private volatile int lastTermCurrentNodeHasVoted = -1;
    
    /**
     * raft config
     */
    protected RaftConfig raftConfig;
    
    /**
     * RaftMemberManager
     */
    protected RaftMemberManager raftMemberManager;
    
    /**
     * FollowerNotifier
     */
    protected AbstractFollowerNotifier abstractFollowerNotifier;
    
    /**
     * storage repository
     */
    private ReplicatedStateMachine replicatedStateMachine;
    
    /**
     * log sender
     */
    protected LogSender logSender;
    
    /**
     * vote executor
     */
    private ExecutorService executorService = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MICROSECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("Ask-Vote-Thread");
            thread.setDaemon(true);
            return thread;
        }
    });
    
    public AbstractRaftServer() {

    }
    
    /**
     * init status of server
     */
    public void start() {
        try {
            // load entity
            Class.forName(MsgUtils.class.getName());
            // start server
            doStartServer();
            // init job to vote
            initAskVoteLeaderJob();
        } catch (Exception e) {
            throw new IllegalStateException("Fail to start raft server, because " + e.getLocalizedMessage());
        }
    }
    
    /**
     * whether current node has just one leader
     *
     * @return whether current node has just one leader
     */
    public boolean normalCluster() {
        return normalCluster.get();
    }
    
    /**
     * ask votes to be leader
     *
     * @throws Exception
     */
    private void initAskVoteLeaderJob() {
        executorService.execute(() -> {
            try {
                while (!executorService.isShutdown()) {
                    // close if leave cluster
                    if (raftMemberManager.isSelfLeave()) {
                        close();
                        return;
                    }
                    // wait a random time
                    int waitTime =
                            raftConfig.getVoteBaseTime() + new Random().nextInt(raftConfig.getVoteBaseTime() / 2);
                    Thread.sleep(waitTime);
                    // No election will be held if the following conditions are met:
                    //   if current node is leader
                    //   if cluster has leader base on leaderLastActiveTime variable
                    //   if current node receive a vote request from other in this term
                    if (isLeader() || raftMemberManager.isSelfLeave()) {
                        continue;
                    }
                    // if follower and timeout
                    boolean leaderHeartbeatTimeout = NodeRole.FOLLOWER.equals(getSelf().getRole())
                            && System.currentTimeMillis() - leaderLastActiveTime > raftConfig
                            .getAbnormalActiveInterval();
                    // if candidate and timeout
                    boolean candidateWaitTimeout = NodeRole.CANDIDATE.equals(getSelf().getRole())
                            && System.currentTimeMillis() - beCandidateStartTime > raftConfig
                            .getCandidateStatusTimeout();
                    if (leaderHeartbeatTimeout || candidateWaitTimeout) {
                        // timeout if cluster invalid
                        normalCluster.set(false);
                        // prevote, try to increase term of current node
                        if (prevote()) {
                            raftMemberManager.getSelf().increaseTerm();
                            LOGGER.info("Server member increase its term, member: " + raftMemberManager.getSelf()
                                    .getAddress() + ", term: " + raftMemberManager.getSelf().getTerm());
                        } else {
                            continue;
                        }
                        // return the val whether don't need to allow to vote again
                        // current may be leader
                        if (requestVote()) {
                            getSelf().setRole(NodeRole.LEADER);
                            // syn log id
                            synAllMember();
                            normalCluster.compareAndSet(false, true);
                            LOGGER.info(
                                    "Server node has been leader, leader: {}, members list: {}", raftMemberManager.getSelf().getAddress(), raftMemberManager.getAllMember());
                            // send no-op
                            Member self = raftMemberManager.getSelf();
                            LogEntry noOp = new LogEntry(self.getTerm(), self.getLogId() + 1,
                                    new Payload(NO_OP_META_DATA, LogType.NO_OP));
                            for (Member member : raftMemberManager.getAllMember()) {
                                try {
                                    logSender.appendLog(member, noOp);
                                } catch (Exception e) {
                                    LOGGER.error("Fail to append no-op log, member: {}, because {}", member.getAddress(), e.getMessage());
                                }
                            }
                            replicatedStateMachine.append(noOp);
                            self.increaseLogId();
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Fail to do vote, because " + e.getLocalizedMessage());
            }
        });
    }
    
    /**
     * whether leader in current cluster
     *
     * @return whether leader in current cluster
     */
    public boolean leaderExisted() {
        return NodeRole.FOLLOWER.equals(getSelf().getRole())
                && System.currentTimeMillis() - leaderLastActiveTime > raftConfig
                .getAbnormalActiveInterval();
    }
    
    /**
     * get self
     *
     * @return self
     */
    public abstract Member getSelf();
    
    /**
     * update member term and log id
     */
    private void synAllMember() {
        List<Member> allMember = raftMemberManager.getAllMember();
        Member self = raftMemberManager.getSelf();
        allMember.forEach(member -> {
            member.setTerm(self.getTerm());
            member.setLogId(replicatedStateMachine.getLastUncommittedLogId());
        });
    }
    
    /**
     * start server
     */
    protected abstract void doStartServer();
    
    /**
     * request for leader
     *
     * @return whether current node being leader
     * @throws Exception exception
     */
    protected abstract boolean requestVote() throws Exception;
    
    /**
     * prevote to increase term of current node
     *
     * @return whether success
     * @throws Exception exception
     */
    protected abstract boolean prevote() throws Exception;
    
    /**
     * whether current node is leader
     *
     * @return whether current node is leader
     */
    public abstract boolean isLeader();
    
    /**
     * refresh if leader active
     */
    public synchronized void refreshLeaderActive() {
        raftMemberManager.getSelf().setRole(NodeRole.FOLLOWER);
        leaderLastActiveTime = System.currentTimeMillis();
        normalCluster.compareAndSet(false, true);
    }
    
    /**
     * invoke if current node vote for other node
     */
    public synchronized void refreshCandidateActive() {
        raftMemberManager.getSelf().setRole(NodeRole.CANDIDATE);
        beCandidateStartTime = System.currentTimeMillis();
        normalCluster.compareAndSet(false, true);
    }
    
    /**
     * refresh last voted term
     *
     * @param term term has voted
     */
    public synchronized void refreshLastVotedTerm(int term) {
        lastTermCurrentNodeHasVoted = term;
        leaderLastActiveTime = System.currentTimeMillis();
        normalCluster.compareAndSet(false, true);
    }
    
    /**
     * get last term has voted
     *
     * @return last term has voted
     */
    public int getLastTermCurrentNodeHasVoted() {
        return lastTermCurrentNodeHasVoted;
    }

    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }

    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }

    public void setAbstractFollowerNotifier(AbstractFollowerNotifier abstractFollowerNotifier) {
        this.abstractFollowerNotifier = abstractFollowerNotifier;
    }

    public void setLogSender(LogSender logSender) {
        this.logSender = logSender;
    }

    public void setRaftConfig(RaftConfig raftConfig) {
        this.raftConfig = raftConfig;
    }

    /**
     * close
     */
    public void close() {
        Member self = raftMemberManager.getSelf();
        raftMemberManager.close();
        abstractFollowerNotifier.close();
        replicatedStateMachine.close();
        LOGGER.info("Server: {} Close finish!", self.getAddress());
    }
}
