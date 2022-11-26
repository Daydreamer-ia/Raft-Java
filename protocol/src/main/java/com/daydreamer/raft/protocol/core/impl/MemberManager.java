package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcConnection;
import com.daydreamer.raft.api.grpc.RequesterGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daydreamer
 * <p>
 * storge member
 */
public class MemberManager implements RaftMemberManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberManager.class.getSimpleName());
    
    private static final String IP_PORT_ADDR_FORMAT = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:[0-9]{2,5}";
    
    private PropertiesReader<RaftConfig> propertiesReader;
    
    private RaftConfig raftConfig;
    
    private Member self;
    
    private List<Member> members = new ArrayList<>();
    
    public MemberManager(PropertiesReader<RaftConfig> propertiesReader) {
        this.propertiesReader = propertiesReader;
        this.raftConfig = propertiesReader.getProperties();
    }
    
    /**
     * init current node member
     */
    public void initSelf() {
        try {
            String serverAddr = raftConfig.getServerAddr();
            Member tmp = buildRawMember(serverAddr);
            tmp.setTerm(0);
            tmp.setLogId(-1);
            tmp.setStatus(NodeStatus.UP);
            self = tmp;
        } catch (Exception e) {
            throw new IllegalStateException("[MemberManager] - Fail to init self message!");
        }
    }
    
    @Override
    public void init() {
        // init self
        initSelf();
        // load member
        List<String> memberAddresses = raftConfig.getMemberAddresses();
        for (String addr : memberAddresses) {
            Member member = buildRawMember(addr);
            members.add(member);
        }
        // create connection
        for (Member member : members) {
            member.setConnection(createConnection(member));
        }
    }
    
    /**
     * get raw member
     *
     * @param addr member address
     * @return new member
     */
    private Member buildRawMember(String addr) {
        Member member = new Member();
        member.setStatus(NodeStatus.DOWN);
        member.setRole(NodeRole.FOLLOWER);
        member.setAddress(addr);
        if (addr.matches(IP_PORT_ADDR_FORMAT)) {
            String[] split = addr.split(":");
            member.setIp(split[0]);
            member.setPort(Integer.parseInt(split[1]));
        }
        return member;
    }
    
    /**
     * create connection
     *
     * @param member target host
     * @return conn
     */
    private Connection createConnection(Member member) {
        // init channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(member.getIp(), member.getPort())
                .usePlaintext()
                .build();
        // init service rpc Stub
        RequesterGrpc.RequesterBlockingStub blockingStub = RequesterGrpc.newBlockingStub(channel);
        return new GrpcConnection(member.getAddress(), blockingStub);
    }
    
    @Override
    public List<Member> getAllMember() {
        return Collections.unmodifiableList(members);
    }
    
    @Override
    public List<Member> getActiveMember() {
        return members.stream().filter(member -> NodeStatus.UP.equals(member.getStatus())).collect(Collectors.toList());
    }
    
    @Override
    public boolean addNewMember(String addr) {
        Member member = buildRawMember(addr);
        member.setConnection(createConnection(member));
        members.add(member);
        LOGGER.info("Add new member, member: {}, current members list: {}", member, members);
        return true;
    }
    
    @Override
    public boolean removeMember(String id) {
        boolean remove =  members.removeIf(member -> {
            if (member.getAddress().equals(id)) {
                member.getConnection().close();
                return true;
            }
            return false;
        });
        if (remove) {
            LOGGER.info("Remove member: {}, current members list: {}", id, members);
        }
        return remove;
    }
    
    @Override
    public Member getSelf() {
        return self;
    }
    
    @Override
    public Member getMemberById(String id) {
        return null;
    }
    
    @Override
    public boolean isLeader() {
        return NodeRole.LEADER.equals(self.getRole());
    }
    
    @Override
    public boolean isSelfLeave() {
        return members.isEmpty();
    }
    
    @Override
    public void removeSelf() {
        close();
        members.clear();
        LOGGER.info("Current member: {} has leave cluster", self.getAddress());
        // down to follower
        self.setRole(NodeRole.FOLLOWER);
    }
    
    @Override
    public void close() {
        getAllMember().forEach(member -> {
            if (member.getConnection() != null) {
                member.getConnection().close();
            }
        });
    }
}
