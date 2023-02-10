package org.daydreamer.persistence.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.entity.RaftConfig;
import org.daydreamer.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SPIImplement("fileSystem")
public class FileSystemPersistence implements PersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemPersistence.class);

    /**
     * config
     */
    private RaftConfig raftConfig;

    private static final String DIR_FILE = "ddr_raft";

    private static final String LOG_STORAGE_FILE = "ddr_raft_log_data";

    private ObjectOutputStream logAppendStream;

    /**
     * whether enable persistence
     */
    private boolean enabled;

    @SPIMethodInit
    private void init() {
        try {
            // create if not exist
            enabled = raftConfig.isPersistent();
            // load
            if (enabled) {
                File baseDir = new File(raftConfig.getDataDir());
                if (!baseDir.exists()) {
                    throw new IllegalArgumentException("Not specify the data dir or data dir is not exists!");
                }
                // create dir
                File dataDir = new File(raftConfig.getDataDir() + File.separator + DIR_FILE);
                if (!dataDir.exists()) {
                    createNewDir(dataDir);
                }
                String address = raftConfig.getServerAddr().replace(":", "_");
                File dataFile = new File(raftConfig.getDataDir() + File.separator + DIR_FILE
                        + File.separator + LOG_STORAGE_FILE + "_" + address);
                if (!dataFile.exists()) {
                    createNewFile(dataFile);
                }
                // append to log file
                logAppendStream = new ObjectOutputStream(new FileOutputStream(dataFile, true));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void createNewFile(File file) throws IOException {
        boolean newFile = file.createNewFile();
        if (!newFile) {
            throw new IllegalStateException("Cannot create file for log storage!");
        }
    }

    public void createNewDir(File file) {
        boolean mkdir = file.mkdir();
        if (!mkdir) {
            throw new IllegalStateException("Cannot create dir for data dir!");
        }
    }

    @Override
    public boolean write(LogEntry logEntry) {
        // if persistent
        try {
            if (this.enabled) {
                logAppendStream.writeObject(logEntry);
                logAppendStream.flush();
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Fail to persist log: {} to disk", logEntry);
        }
        return false;
    }

    @Override
    public List<LogEntry> read() {
        LinkedList<LogEntry> res = new LinkedList<>();
        if (this.enabled) {
            try {
                String address = raftConfig.getServerAddr().replace(":", "_");
                File dataFile = new File(raftConfig.getDataDir() + File.separator + DIR_FILE
                        + File.separator + LOG_STORAGE_FILE + "_" + address);
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFile));
                while (true) {
                    try {
                        Object o = objectInputStream.readObject();
                        if (o instanceof LogEntry) {
                            res.addLast((LogEntry) o);
                        }
                    } catch (EOFException e) {
                        // nothing to load
                        return res;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Fail to load log snapshot file, because {}", e.getLocalizedMessage());
                return Collections.emptyList();
            }
        }
        return res;
    }

    public void setRaftConfig(RaftConfig raftConfig) {
        this.raftConfig = raftConfig;
    }
}
