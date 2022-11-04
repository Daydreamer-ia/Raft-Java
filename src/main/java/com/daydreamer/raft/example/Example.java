package com.daydreamer.raft.example;

import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.core.impl.RaftProtocol;

/**
 * @author Daydreamer
 */
public class Example {
    
    public static void main(String[] args) throws InterruptedException {
        /*
         * start three application with different config to simulate cluster
         */
        Protocol raft = new RaftProtocol("src/main/resources/example-server0.properties");
//        Protocol raft = new RaftProtocol("src/main/resources/example-server1.properties");
//        Protocol raft = new RaftProtocol("src/main/resources/example-server2.properties");
        raft.run();
        Thread.sleep(120 * 1000);
    }
}
