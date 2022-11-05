package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.common.utils.MsgUtils;
import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.core.impl.RaftPropertiesReader;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;
import com.daydreamer.raft.transport.connection.Closeable;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Daydreamer
 */
public abstract class AbstractRaftServer implements Closeable {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractRaftServer.class.getSimpleName());
    
    /**
     * if there is a leader in cluster
     */
    protected AtomicBoolean normalCluster = new AtomicBoolean(false);
    
    /**
     * last time when leader active
     *
     * update if current node is follower and receive leader heartbeat
     */
    protected volatile long leaderLastActiveTime;
    
    /**
     * to be candidate start time
     *
     * update if current node is follower and receive other node vote request
     */
    protected volatile long beCandidateStartTime;
    
    /**
     * last term current node has voted
     */
    private volatile int lastTermCurrentNodeHasVoted;
    
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
    protected FollowerNotifier followerNotifier;
    
    /**
     * raft properties
     */
    private RaftPropertiesReader raftPropertiesReader;
    
    /**
     * vote executor
     */
    private ExecutorService executorService = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("Ask-Vote-Thread");
            thread.setDaemon(true);
            return thread;
        }
    });
    
    public AbstractRaftServer(RaftPropertiesReader raftPropertiesReader, RaftMemberManager raftMemberManager,
            FollowerNotifier followerNotifier) {
        this.raftPropertiesReader = raftPropertiesReader;
        this.raftMemberManager = raftMemberManager;
        this.followerNotifier = followerNotifier;
        this.raftConfig = raftPropertiesReader.getProperties();
    }
    
    /**
     * init status of server
     */
    public void start() {
        try {
            // init request handler
            RequestHandlerHolder.init(raftMemberManager, followerNotifier, this);
            // load entity
            Class.forName(MsgUtils.class.getName());
            // init member manager
            raftMemberManager.init();
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
                while (true) {
                    // wait a random time
                    int waitTime = raftConfig.getVoteBaseTime() + new Random().nextInt(raftConfig.getVoteBaseTime() / 2);
                    Thread.sleep(waitTime);
                    // No election will be held if the following conditions are met:
                    //   if current node is leader
                    //   if cluster has leader base on leaderLastActiveTime variable
                    //   if current node receive a vote request from other in this term
                    if (isLeader()) {
                        System.out.println("[AbstractRaftServer] - Current node is leader, term: " + raftMemberManager.getSelf().getTerm() + ", member: " + raftMemberManager.getSelf().getIp());
                        continue;
                    }
                    // if follower and timeout
                    boolean leaderHeartbeatTimeout = NodeRole.FOLLOWER.equals(getSelf().getRole()) && System.currentTimeMillis() - leaderLastActiveTime > raftConfig.getAbnormalActiveInterval();
                    // if candidate and timeout
                    boolean candidateWaitTimeout = NodeRole.CANDIDATE.equals(getSelf().getRole()) && System.currentTimeMillis() - beCandidateStartTime > raftConfig.getCandidateStatusTimeout();
                    if (leaderHeartbeatTimeout || candidateWaitTimeout) {
                        // return the val whether don't need to allow to vote again
                        // current may be leader
                        if (requestVote()) {
                            getSelf().setRole(NodeRole.LEADER);
                            normalCluster.compareAndSet(false, true);
                            LOGGER.info("[AbstractRaftServer] - Server node has been leader, member: " + raftMemberManager.getSelf().getAddress());
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("[AbstractRaftServer] - Fail to do vote, because " + e.getLocalizedMessage());
            }
        });
    }
    
    /**
     * get self
     *
     * @return self
     */
    public abstract Member getSelf();
    
    /**
     * start server
     */
    protected abstract void doStartServer();
    
    /**
     * request for leader
     *
     * @return whether current node being leader
     * @throws Exception
     */
    protected abstract boolean requestVote() throws Exception;
    
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
    }
    
    /**
     * refresh last voted term
     *
     * @param term term has voted
     */
    public synchronized void refreshLastVotedTerm(int term) {
        lastTermCurrentNodeHasVoted = term;
    }
    
    /**
     * get last term has voted
     *
     * @return last term has voted
     */
    public int getLastTermCurrentNodeHasVoted() {
        return lastTermCurrentNodeHasVoted;
    }
}
