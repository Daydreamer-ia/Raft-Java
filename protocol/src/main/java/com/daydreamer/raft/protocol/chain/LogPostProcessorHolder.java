package com.daydreamer.raft.protocol.chain;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.protocol.aware.ReplicatedStateMachineAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daydreamer
 * <p>
 * registry for {@link LogPostProcessor}
 */
public class LogPostProcessorHolder implements LogPostProcessor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogPostProcessorHolder.class);
    
    private static final String PROCESSOR_PACKAGE = "com/daydreamer/raft/protocol/chain/impl";
    
    private static final String PACKAGE_SEPARATOR = ".";
    
    private static final String CLASS_FORMAT = ".class";
    
    private static final String EMPTY = "";
    
    private List<LogPostProcessor> postProcessors = new ArrayList<>();
    
    private RaftMemberManager raftMemberManager;
    
    private AbstractRaftServer abstractRaftServer;
    
    private ReplicatedStateMachine replicatedStateMachine;
    
    /**
     * whether init
     */
    private AtomicBoolean finishInit = new AtomicBoolean(false);
    
    
    /**
     * scan package and init
     *
     * @param raftMemberManager  raftMemberManager
     * @param replicatedStateMachine  storageRepository
     * @param abstractRaftServer abstractRaftServer
     */
    public LogPostProcessorHolder(RaftMemberManager raftMemberManager, AbstractRaftServer abstractRaftServer,
            ReplicatedStateMachine replicatedStateMachine) {
        this.raftMemberManager = raftMemberManager;
        this.abstractRaftServer = abstractRaftServer;
        this.replicatedStateMachine = replicatedStateMachine;
        init();
    }
    
    /**
     * init {@link LogPostProcessor}
     */
    private synchronized void init() {
        if (finishInit.get()) {
            return;
        }
        try {
            // load instance
            File file = new File(
                    Objects.requireNonNull(LogPostProcessorHolder.class.getClassLoader().getResource(PROCESSOR_PACKAGE))
                            .getFile());
            File[] files = file.listFiles();
            String packagePrefix = PROCESSOR_PACKAGE.replaceAll("/", ".");
            if (files != null) {
                // register no args constructor handler
                for (File child : files) {
                    String clazzName = packagePrefix + PACKAGE_SEPARATOR + child.getName().replace(CLASS_FORMAT, EMPTY);
                    Class<?> clazz = Class.forName(clazzName);
                    LogPostProcessor logPostProcessor = (LogPostProcessor) clazz.newInstance();
                    injectAware(logPostProcessor);
                    postProcessors.add(logPostProcessor);
                }
                finishInit.set(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Can not load base processor for request", e);
        }
    }
    
    /**
     * inject aware
     *
     * @param postProcessor postProcessor
     */
    private void injectAware(LogPostProcessor postProcessor) {
        if (postProcessor instanceof RaftMemberManagerAware) {
            ((RaftMemberManagerAware) postProcessor).setRaftMemberManager(raftMemberManager);
        }
        if (postProcessor instanceof RaftServerAware) {
            ((RaftServerAware) postProcessor).setRaftServer(abstractRaftServer);
        }
        if (postProcessor instanceof ReplicatedStateMachineAware) {
            ((ReplicatedStateMachineAware) postProcessor).setReplicatedStateMachine(replicatedStateMachine);
        }
    }
    
    /**
     * get all processors
     *
     * @return all processors
     */
    public List<LogPostProcessor> getPostProcessors() {
        return Collections.unmodifiableList(postProcessors);
    }
    
    /**
     * register new processor
     *
     * @param logPostProcessor new processor
     */
    public synchronized void register(LogPostProcessor logPostProcessor) {
        postProcessors.add(logPostProcessor);
    }
    
    @Override
    public boolean handleBeforeAppend(LogEntry logEntry) {
        boolean continueNext = true;
        for (int i = 0; continueNext && i < postProcessors.size(); i++) {
            try {
                continueNext = postProcessors.get(i).handleBeforeAppend(logEntry);
            } catch (Exception e) {
                LOGGER.error("Fail to handle before appending, because {}, log: {}", e.getMessage(), logEntry);
            }
        }
        return continueNext;
    }
    
    @Override
    public void handleAfterAppend(LogEntry logEntry) {
        for (LogPostProcessor postProcessor : postProcessors) {
            try {
                postProcessor.handleBeforeAppend(logEntry);
            } catch (Exception e) {
                LOGGER.error("Fail to handle after appending, because {}, log: {}", e.getMessage(), logEntry);
            }
        }
    }
    
    @Override
    public void handleAfterCommit(LogEntry logEntry) {
        for (LogPostProcessor postProcessor : postProcessors) {
            try {
                postProcessor.handleAfterCommit(logEntry);
            } catch (Exception e) {
                LOGGER.error("Fail to handle after committing, because {}, log: {}", e.getMessage(), logEntry);
            }
        }
    }
    
    @Override
    public boolean handleBeforeCommit(LogEntry logEntry) {
        boolean continueNext = true;
        for (int i = 0; continueNext && i < postProcessors.size(); i++) {
            try {
                continueNext = postProcessors.get(i).handleBeforeCommit(logEntry);
            } catch (Exception e) {
                LOGGER.error("Fail to handle before committing, because {}, log: {}", e.getMessage(), logEntry);
            }
        }
        return continueNext;
    }
}
