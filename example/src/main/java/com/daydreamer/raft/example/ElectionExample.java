package com.daydreamer.raft.example;

import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.core.impl.RaftProtocol;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daydreamer
 */
public class ElectionExample {
    
    public static void main(String[] args) throws InterruptedException {
        /*
         * start three application with different config to simulate cluster
         * then they will elect for leader
         */
        List<RaftProtocol> serverList = Stream
                .of(new RaftProtocol("example-server0.properties"),
                        new RaftProtocol("example-server1.properties"),
                        new RaftProtocol("example-server2.properties"))
                .collect(Collectors.toList());
        // start all
        serverList.forEach(Protocol::run);
        Thread.sleep(120 * 1000);
        // stop all
        serverList.forEach(Protocol::close);
    }
}
