package com.daydreamer.raft.example;

import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.core.impl.RaftProtocol;

/**
 * @author Daydreamer
 */
public class ElectionExample {
    
    public static void main(String[] args) throws InterruptedException {
        /*
         * start three application with different config to simulate cluster
         * then they will elect for leader
         */
        Protocol raft = new RaftProtocol("example-server0.properties");
        Protocol raft1 = new RaftProtocol("example-server1.properties");
        Protocol raft2 = new RaftProtocol("example-server2.properties");
        raft.run();
        raft1.run();
        raft2.run();
        Thread.sleep(120 * 1000);
    }
}
