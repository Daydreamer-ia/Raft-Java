package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.constant.ResponseCode;
import com.daydreamer.raft.api.entity.request.EntryCommittedRequest;
import com.daydreamer.raft.api.entity.response.EntryCommittedResponse;
import com.daydreamer.raft.api.entity.response.ServerErrorResponse;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public class LogCommittedRequestHandler
        implements RequestHandler<EntryCommittedRequest, Response>, StorageRepositoryAware {
    
    private static final Logger LOGGER = Logger.getLogger(LogCommittedRequestHandler.class);
    
    private ReplicatedStateMachine replicatedStateMachine;
    
    @Override
    public synchronized Response handle(EntryCommittedRequest request) {
        try {
            long logId = request.getLogId();
            int term = request.getTerm();
            // commit if not committed
            if (replicatedStateMachine.getLastUncommittedLogId() >= request.getLogId()) {
                LogEntry logEntry = replicatedStateMachine.getLogById(logId);
                if (logEntry != null && logEntry.getTerm() == term) {
                    replicatedStateMachine.commit(term, logId);
                }
            }
            return new EntryCommittedResponse(true);
        } catch (Exception e) {
            LOGGER.error("Fail to commit log, because: "+ e.getMessage());
            return new ServerErrorResponse(e.getMessage(), ResponseCode.ERROR_SERVER);
        }
    }
    
    @Override
    public Class<EntryCommittedRequest> getSource() {
        return EntryCommittedRequest.class;
    }
    
    @Override
    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }
}
