package org.daydreamer.persistence;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPI;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * log storage
 */
@SPI("fileSystem")
public interface PersistenceService {

    /**
     * write lof if commit
     *
     * @param logEntry committed log
     */
    boolean write(LogEntry logEntry);

    /**
     * read all committed log
     *
     * @return all committed logs
     */
    List<LogEntry> read();

}
