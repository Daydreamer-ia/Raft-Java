package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.constant.RaftProperty;
import com.daydreamer.raft.common.entity.RaftConfig;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public class RaftPropertiesReader extends PropertiesReader<RaftConfig> {

    private static final Logger LOGGER = Logger.getLogger(RaftPropertiesReader.class);

    private static final AtomicBoolean init = new AtomicBoolean(false);

    public RaftPropertiesReader(String filePath) {
        super(filePath, new RaftConfig(), true);
    }

    public RaftPropertiesReader(RaftConfig raftConfig) {
        super(null, raftConfig, false);
    }

    @Override
    public void populateProperties(Properties properties, RaftConfig activeProperties) {
        try {
            String members = properties.getProperty(RaftProperty.MEMBER_ADDRESSES);
            if (StringUtils.isNotBlank(members) && activeProperties.getMemberAddresses() == null) {
                String[] split = members.split(", ");
                List<String> addresses = new ArrayList<>(split.length);
                for (String addr : split) {
                    addresses.add(addr);
                }
                activeProperties.setMemberAddresses(addresses);
            } else {
                LOGGER.warn("Current version don't allow to change members, excuse please!");
            }
            String heartbeat = properties.getProperty(RaftProperty.LEADER_HEARTBEAT);
            if (StringUtils.isNotBlank(heartbeat)) {
                int h = Integer.parseInt(heartbeat);
                activeProperties.setHeartInterval(h);
            }
            String abnormalInternal = properties.getProperty(RaftProperty.ABNORMAL_LEADER_ACTIVE_INTERNAL);
            if (StringUtils.isNotBlank(abnormalInternal)) {
                int h = Integer.parseInt(abnormalInternal);
                activeProperties.setAbnormalActiveInterval(h);
            }
            String voteBaseTime = properties.getProperty(RaftProperty.VOTE_BASE_TIME);
            if (StringUtils.isNotBlank(voteBaseTime)) {
                int h = Integer.parseInt(voteBaseTime);
                activeProperties.setVoteBaseTime(h);
            }
            String candidateTimeout = properties.getProperty(RaftProperty.CANDIDATE_WAIT_TIMEOUT);
            if (candidateTimeout != null) {
                int h = Integer.parseInt(candidateTimeout);
                activeProperties.setCandidateStatusTimeout(h);
            }
            String retryWrite = properties.getProperty(RaftProperty.WRITE_RETRY_TIMES_IF_FAIL);
            if (StringUtils.isNotBlank(retryWrite)) {
                int rw = Integer.parseInt(retryWrite);
                activeProperties.setWriteRetryTimes(rw);
            }
            String serverAddr = properties.getProperty(RaftProperty.SERVER_ADDR);
            if (StringUtils.isNotBlank(serverAddr) && activeProperties.getServerAddr() == null) {
                activeProperties.setServerAddr(serverAddr);
            } else {
                LOGGER.warn("Server address cannot be changed while running!");
            }
            String rejectWrite = properties.getProperty(RaftProperty.REJECT_WRITE_IF_FOLLOWER);
            if (StringUtils.isNotBlank(rejectWrite)) {
                activeProperties.setFollowerRejectWrite(!rejectWrite.contains("false"));
            }
            String defaultPoolCore = properties.getProperty(RaftProperty.DEFAULT_THREAD_POOL_CORE);
            String defaultPoolMax = properties.getProperty(RaftProperty.DEFAULT_THREAD_POOL_MAX);
            if (StringUtils.isNotBlank(defaultPoolCore) && StringUtils.isNotBlank(defaultPoolMax)) {
                activeProperties.setDefaultThreadPoolMaxThread(Integer.parseInt(defaultPoolMax));
                activeProperties.setDefaultThreadPoolCoreThread(Integer.parseInt(defaultPoolCore));
            }
            String enablePersistenceStr = properties.getProperty(RaftProperty.LOG_PERSISTENT);
            boolean enablePersistence = getValue(() -> Boolean.valueOf(enablePersistenceStr),
                    true, "Fail to load enablePersistence");
            activeProperties.setPersistent(enablePersistence);
            String dataDir = properties.getProperty(RaftProperty.LOG_DATA_DIR);
            if (!init.get() && dataDir != null) {
                activeProperties.setDataDir(dataDir);
            }
            init.set(true);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Fail to update properties, because: " + e.getMessage());
        }
    }

    private static <T> T getValue(Supplier<T> supplier, T defaultVal, String errorMsg) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.error(errorMsg + ", because" + e.getLocalizedMessage());
            return defaultVal;
        }
    }
}
