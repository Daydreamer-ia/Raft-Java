package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcConnection;
import com.daydreamer.raft.transport.grpc.RequesterGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daydreamer
 *
 * storge member
 */
public class MemberManager implements RaftMemberManager {
    
    private static final String IP_PORT_ADDR_FORMAT = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:[0-9]{2,5}";
    
    private RaftConfig raftConfig;
    
    private Member self;
    
    private List<Member> members = new ArrayList<>();
    
    public MemberManager(RaftConfig raftConfig) {
        this.raftConfig = raftConfig;
    }
    
    /**
     * init current node member
     *
     */
    public void initSelf() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            int port = raftConfig.getPort();
            Member tmp = new Member();
            tmp.setIp(ip);
            tmp.setPort(port);
            tmp.setAddress(ip + ":" + port);
            tmp.setRole(NodeRole.CANDIDATE, null);
            tmp.setMemberId(ip + ":" + port);
            tmp.setStatus(NodeStatus.UP);
            tmp.setTerm(0);
            tmp.setLogId(0);
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
            Member member = new Member();
            member.setStatus(NodeStatus.DOWN);
            member.setRole(NodeRole.FOLLOWER);
            member.setAddress(addr);
            if (addr.matches(IP_PORT_ADDR_FORMAT)) {
                String[] split = addr.split(":");
                member.setIp(split[0]);
                member.setPort(Integer.parseInt(split[1]));
            }
            members.add(member);
        }
        // create connection
        for (Member member : members) {
            member.setConnection(createConnection(member));
        }
    }
    
    /**
     * create connection
     *
     * @param member target host
     * @return conn
     */
    private Connection createConnection(Member member) {
        //初始化连接
        ManagedChannel channel = ManagedChannelBuilder.forAddress(member.getIp(), member.getPort())
                .usePlaintext()
                .build();
        //初始化远程服务Stub
        RequesterGrpc.RequesterBlockingStub blockingStub = RequesterGrpc.newBlockingStub(channel);
        return new GrpcConnection(member.getAddress(), blockingStub);
    }
    
    @Override
    public List<Member> getAllMember() {
        return Collections.unmodifiableList(members);
    }
    
    @Override
    public List<Member> getActiveMember() {
        return members.stream()
                .filter(member -> NodeStatus.UP.equals(member.getStatus()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean addNewMember(Member member) {
        throw new UnsupportedOperationException("Current version don't support member change!");
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
}
