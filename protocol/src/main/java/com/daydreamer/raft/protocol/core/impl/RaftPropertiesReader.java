package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.constant.RaftProperty;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public class RaftPropertiesReader extends PropertiesReader<RaftConfig> {
    
    private static final Logger LOGGER = Logger.getLogger(RaftPropertiesReader.class);
    
    public RaftPropertiesReader(String filePath) {
        super(filePath, new RaftConfig());
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
            String port = properties.getProperty(RaftProperty.SERVER_PORT);
            if (StringUtils.isNotBlank(port) && activeProperties.getPort() == 0) {
                int p = Integer.parseInt(port);
                activeProperties.setPort(p);
            } else {
                LOGGER.warn(
                        "Server port cannot be modified! You can restart server if you want to take it effect.");
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
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Fail to update properties, because: " + e.getMessage());
        }
    }
}
