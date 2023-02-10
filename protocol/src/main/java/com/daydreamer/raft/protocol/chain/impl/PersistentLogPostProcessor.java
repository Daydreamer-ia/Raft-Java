package com.daydreamer.raft.protocol.chain.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.protocol.chain.LogPostProcessor;
import org.daydreamer.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daydreamer
 */
@SPIImplement("persistentLogPostProcessor")
public class PersistentLogPostProcessor implements LogPostProcessor, GroupAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentLogPostProcessor.class);

    private PersistenceService persistenceService;

    private String groupKey;

    @SPIMethodInit
    private void init() {
        persistenceService = RaftServiceLoader.getLoader(groupKey, PersistenceService.class).getDefault();
    }

    @Override
    public boolean handleBeforeCommit(LogEntry logEntry) {
        return persistenceService.write(logEntry);
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
