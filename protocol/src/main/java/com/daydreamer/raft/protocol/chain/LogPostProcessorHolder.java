package com.daydreamer.raft.protocol.chain;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * @author Daydreamer
 * <p>
 * registry for {@link LogPostProcessor}
 */
@SPIImplement("logPostProcessor")
public class LogPostProcessorHolder implements LogPostProcessor, GroupAware {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogPostProcessorHolder.class);
    
    private final List<LogPostProcessor> postProcessors = new ArrayList<>();

    private String groupKey;

    public LogPostProcessorHolder() {

    }

    @SPIMethodInit
    private void init() {
        List<LogPostProcessor> all = RaftServiceLoader.getLoader(groupKey, LogPostProcessor.class).getAll();
        postProcessors.addAll(all);
        postProcessors.remove(this);
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

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
