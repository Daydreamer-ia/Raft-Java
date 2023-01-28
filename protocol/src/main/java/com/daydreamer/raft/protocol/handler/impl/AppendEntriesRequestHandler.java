package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.constant.ResponseCode;
import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.api.entity.response.ServerErrorResponse;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Daydreamer
 */
@SPIImplement("appendEntriesRequestHandler")
public class AppendEntriesRequestHandler
        implements RequestHandler<AppendEntriesRequest, Response>{
    
    private static final Logger LOGGER = Logger.getLogger(AppendEntriesRequestHandler.class);
    
    /**
     * log repository
     */
    private ReplicatedStateMachine replicatedStateMachine;
    
    /**
     * raft member manager
     */
    private RaftMemberManager raftMemberManager;
    
    @Override
    public synchronized Response handle(AppendEntriesRequest request) {
        try {
            // check log id and leaderLastLogTerm
            int leaderLastLogTerm = request.getLastTerm();
            long leaderLastLogId = request.getLastLogId();
            // if first log
            List<LogEntry> logEntries = request.getLogEntries();
            if (leaderLastLogId == -1) {
                if (replicatedStateMachine.getLastCommittedLogId() == -1) {
                    boolean append = false;
                    for (LogEntry logEntry : logEntries) {
                        append = replicatedStateMachine.append(logEntry);
                        if (!append) {
                            break;
                        }
                    }
                    if (append) {
                        // update log index
                        raftMemberManager.getSelf().setLogId(logEntries.get(logEntries.size() - 1).getLogId());
                    }
                    return new AppendEntriesResponse(append);
                } else {
                    LOGGER.error("First log has committed, cannot cover!");
                    return new ServerErrorResponse("Log has committed", ResponseCode.ERROR_CLIENT);
                }
            }
            // try to find
            LogEntry lastLog = replicatedStateMachine.getLogById(leaderLastLogId);
            // if uncommitted, try to append
            if (replicatedStateMachine.getLastCommittedLogId() < logEntries.get(logEntries.size() - 1).getLogId()) {
                // if found, then append
                if (lastLog != null && lastLog.getTerm() == leaderLastLogTerm) {
                    // append all
                    boolean append = false;
                    for (LogEntry logEntry : logEntries) {
                        append = replicatedStateMachine.append(logEntry);
                        if (!append) {
                            break;
                        }
                    }
                    // update log index
                    raftMemberManager.getSelf().setLogId(logEntries.get(logEntries.size() - 1).getLogId());
                    return new AppendEntriesResponse(append);
                }
                // cannot found, then ask leader to syn ahead
                else {
                    return new AppendEntriesResponse(false);
                }
            }
            // if has committed
            else {
                return new AppendEntriesResponse(true);
            }
        } catch (Exception e) {
            // nothing to do
            LogEntry lastCommittedLog = replicatedStateMachine
                    .getCommittedLog(replicatedStateMachine.getLastCommittedLogId());
            LOGGER.error("Fail to append log, leader last term: " + request.getLastTerm() + ", leader last log id: "
                    + request.getLastLogId() + ", current node committed log term: " + lastCommittedLog.getTerm()
                    + ", current node committed log id: " + lastCommittedLog.getLogId());
            return new ServerErrorResponse(e.getMessage(), ResponseCode.ERROR_SERVER);
        }
    }
    
    @Override
    public Class<AppendEntriesRequest> getSource() {
        return AppendEntriesRequest.class;
    }

    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }

    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
}
