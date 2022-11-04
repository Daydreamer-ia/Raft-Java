package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;

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
        // init
        init();
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
