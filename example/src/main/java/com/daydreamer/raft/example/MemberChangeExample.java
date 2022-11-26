package com.daydreamer.raft.example;

import com.daydreamer.raft.api.entity.base.MemberChangeEntry;
import com.daydreamer.raft.api.entity.constant.MemberChange;
import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.core.impl.RaftProtocol;
import com.daydreamer.raft.protocol.entity.RaftConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daydreamer
 */
public class MemberChangeExample {
    
    public static void main(String[] args) throws InterruptedException {
        List<Protocol> protocols = createRaftProtocol();
        protocols.forEach(Protocol::run);
        Thread thread = new Thread(getTask(protocols));
        thread.start();
        // wait for member changing, remove node: 127.0.0.1:17999
        Thread.sleep(60 * 1000);
        protocols.forEach(Protocol::close);
    }
    
    /**
     * job to append log
     *
     * @return task
     */
    public static Runnable getTask(List<Protocol> protocols) {
        return  () -> {
            try {
                // wait for cluster ready (to do leader election)
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                // nothing to do
            }
            System.out.println("Try to change member, remove server: 127.0.0.1:17999");
            // try to write
            for (Protocol protocol : protocols) {
                // find leader
                try {
                    MemberChangeEntry action = new MemberChangeEntry("127.0.0.1:17999", MemberChange.REMOVE);
                    boolean success = protocol.memberChange(action);
                    System.out.println("operation result: " + success);
                } catch (IllegalStateException e) {
                    // it not leader
                } catch (Exception e) {
                    // nothing to do
                }
            }
        };
    }
    
    public static List<Protocol> createRaftProtocol() {
        List<Protocol> protocols = new ArrayList<>();
        List<RaftConfig> raftConfig = createRaftConfig();
        for (RaftConfig config : raftConfig) {
            protocols.add(new RaftProtocol(config));
        }
        return protocols;
    }
    
    
    public static List<RaftConfig> createRaftConfig() {
        // server01
        RaftConfig raftConfig1 = new RaftConfig();
        raftConfig1.setServerAddr("127.0.0.1:17999");
        raftConfig1.setMemberAddresses(Stream.of("127.0.0.1:18999", "127.0.0.1:19999").collect(Collectors.toList()));
        // server02
        RaftConfig raftConfig2 = new RaftConfig();
        raftConfig2.setServerAddr("127.0.0.1:18999");
        raftConfig2.setMemberAddresses(Stream.of("127.0.0.1:17999", "127.0.0.1:19999").collect(Collectors.toList()));
        // server03
        RaftConfig raftConfig3 = new RaftConfig();
        raftConfig3.setServerAddr("127.0.0.1:19999");
        raftConfig3.setMemberAddresses(Stream.of("127.0.0.1:17999", "127.0.0.1:18999").collect(Collectors.toList()));
        return Stream.of(raftConfig1, raftConfig2,raftConfig3).collect(Collectors.toList());
    }
}
