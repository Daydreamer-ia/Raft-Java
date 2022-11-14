package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.common.utils.MsgUtils;
import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;
import com.daydreamer.raft.protocol.storage.StorageRepository;
import com.daydreamer.raft.transport.connection.Closeable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public abstract class AbstractRaftServer {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractRaftServer.class);
    
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
    private StorageRepository storageRepository;
    
    /**
     * registry
     */
    protected RequestHandlerHolder requestHandlerHolder;
    
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
    
    public AbstractRaftServer(RaftConfig raftConfig, RaftMemberManager raftMemberManager,
            AbstractFollowerNotifier abstractFollowerNotifier, StorageRepository storageRepository) {
        this.raftConfig = raftConfig;
        this.raftMemberManager = raftMemberManager;
        this.abstractFollowerNotifier = abstractFollowerNotifier;
        this.storageRepository = storageRepository;
    }
    
    /**
     * init status of server
     */
    public void start() {
        try {
            // init request handler
            requestHandlerHolder = new RequestHandlerHolder(raftMemberManager, this, storageRepository);
            // load entity
            Class.forName(MsgUtils.class.getName());
            // init member manager
            raftMemberManager.init();
            // init holder
            requestHandlerHolder.init();
            // start server
            doStartServer();
            // init notify job
            abstractFollowerNotifier.init();
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
                    int waitTime =
                            raftConfig.getVoteBaseTime() + new Random().nextInt(raftConfig.getVoteBaseTime() / 2);
                    Thread.sleep(waitTime);
                    // No election will be held if the following conditions are met:
                    //   if current node is leader
                    //   if cluster has leader base on leaderLastActiveTime variable
                    //   if current node receive a vote request from other in this term
                    if (isLeader()) {
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
                            LOGGER.info("Server member increase its term, member: " + raftMemberManager.getSelf().getAddress() + ", term: " + raftMemberManager.getSelf().getTerm());
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
                                    "Server node has been leader, member: " + raftMemberManager.getSelf().getAddress());
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Fail to do vote, because " + e.getLocalizedMessage());
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
     * update member term and log id
     */
    private void synAllMember() {
        List<Member> allMember = raftMemberManager.getAllMember();
        Member self = raftMemberManager.getSelf();
        allMember.forEach(member -> {
            member.setTerm(self.getTerm());
            member.setLogId(storageRepository.getLastUncommittedLogId());
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
    
    /**
     * close
     */
    public void close() {
        raftMemberManager.close();
        abstractFollowerNotifier.close();
        storageRepository.close();
    }
}
