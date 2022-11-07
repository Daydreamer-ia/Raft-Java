package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.StorageRepository;

import java.util.logging.Logger;

/**
 * @author Daydreamer
 */
public class AppendEntriesRequestHandler
        implements RequestHandler<AppendEntriesRequest, AppendEntriesResponse>, StorageRepositoryAware {
    
    private static final Logger LOGGER = Logger.getLogger(AppendEntriesRequestHandler.class.getSimpleName());
    
    /**
     * log repository
     */
    private StorageRepository storageRepository;
    
    @Override
    public synchronized AppendEntriesResponse handle(AppendEntriesRequest request) {
        try {
            // check log id and leaderLastLogTerm
            int leaderLastLogTerm = request.getLastTerm();
            long leaderLastLogId = request.getLastLogId();
            // try to find
            LogEntry lastLog = storageRepository.getLogById(leaderLastLogId);
            // if found, then append
            if (lastLog != null && lastLog.getTerm() == leaderLastLogTerm) {
                // append all
                for (LogEntry logEntry : request.getLogEntries()) {
                    storageRepository.append(logEntry);
                }
            }
            // cannot found, then ask leader to syn ahead
            else {
                return new AppendEntriesResponse(false);
            }
        } catch (Exception e) {
            // nothing to do
            e.printStackTrace();
            LogEntry lastCommittedLog = storageRepository.getCommittedLog(storageRepository.getLastCommittedLogId());
            LOGGER.severe("[AppendEntriesRequestHandler] - Fail to append log, leader last term: "
                    + request.getLastTerm() + ", leader last log id: " + request.getLastLogId()
                    + ", current node committed log term: " + lastCommittedLog.getTerm()
                    + ", current node committed log id: " + lastCommittedLog.getLogId());
        }
        return new AppendEntriesResponse(false);
    }
    
    @Override
    public Class<AppendEntriesRequest> getSource() {
        return AppendEntriesRequest.class;
    }
    
    @Override
    public void setStorageRepository(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }
}
