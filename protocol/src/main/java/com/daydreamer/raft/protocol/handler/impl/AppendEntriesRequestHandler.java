package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.constant.ResponseCode;
import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.api.entity.response.ServerErrorResponse;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public class AppendEntriesRequestHandler
        implements RequestHandler<AppendEntriesRequest, Response>, StorageRepositoryAware {
    
    private static final Logger LOGGER = Logger.getLogger(AppendEntriesRequestHandler.class);
    
    /**
     * log repository
     */
    private ReplicatedStateMachine replicatedStateMachine;
    
    @Override
    public synchronized Response handle(AppendEntriesRequest request) {
        try {
            // check log id and leaderLastLogTerm
            int leaderLastLogTerm = request.getLastTerm();
            long leaderLastLogId = request.getLastLogId();
            // if first log
            if (leaderLastLogId == -1) {
                if (replicatedStateMachine.getLastCommittedLogId() == -1) {
                    for (LogEntry logEntry : request.getLogEntries()) {
                        replicatedStateMachine.append(logEntry);
                    }
                    return new AppendEntriesResponse(true);
                } else {
                    LOGGER.error("First log has committed, cannot cover!");
                    return new ServerErrorResponse("Log has committed", ResponseCode.ERROR_CLIENT);
                }
            }
            // try to find
            LogEntry lastLog = replicatedStateMachine.getLogById(leaderLastLogId);
            // if found, then append
            if (lastLog != null && lastLog.getTerm() == leaderLastLogTerm) {
                // append all
                for (LogEntry logEntry : request.getLogEntries()) {
                    replicatedStateMachine.append(logEntry);
                }
                return new AppendEntriesResponse(true);
            }
            // cannot found, then ask leader to syn ahead
            else {
                return new AppendEntriesResponse(false);
            }
        } catch (Exception e) {
            // nothing to do
            e.printStackTrace();
            LogEntry lastCommittedLog = replicatedStateMachine.getCommittedLog(replicatedStateMachine.getLastCommittedLogId());
            LOGGER.error(
                    "Fail to append log, leader last term: " + request.getLastTerm()
                            + ", leader last log id: " + request.getLastLogId() + ", current node committed log term: "
                            + lastCommittedLog.getTerm() + ", current node committed log id: " + lastCommittedLog
                            .getLogId());
            return new ServerErrorResponse(e.getMessage(), ResponseCode.ERROR_SERVER);
        }
    }
    
    @Override
    public Class<AppendEntriesRequest> getSource() {
        return AppendEntriesRequest.class;
    }
    
    @Override
    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }
}
