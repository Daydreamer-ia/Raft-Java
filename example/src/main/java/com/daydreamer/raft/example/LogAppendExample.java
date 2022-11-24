package com.daydreamer.raft.example;

import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.constant.LogType;
import com.daydreamer.raft.protocol.core.Protocol;
import com.daydreamer.raft.protocol.core.impl.RaftProtocol;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daydreamer
 */
public class LogAppendExample {
    
    public static void main(String[] args) throws InterruptedException {
        List<Protocol> cluster = getCluster();
        // start
        cluster.forEach(Protocol::run);
        // append log if wait until 30 sec
        Thread appendLogThread = new Thread(getTask(cluster));
        appendLogThread.start();
        // wait for append
        Thread.sleep(60 * 1000);
        // close
        cluster.forEach(Protocol::close);
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
            System.out.println("Try to write!");
            // try to write
            for (Protocol protocol : protocols) {
                // find leader
                try {
                    boolean write = protocol.write(new Payload<>("Hello, raft", LogType.WRITE));
                    System.out.println("write result:" + write);
                } catch (IllegalStateException e) {
                    // it not leader
                } catch (Exception e) {
                    // nothing to do
                }
            }
        };
    }
    
    /**
     * get cluster
     *
     * @return cluster
     */
    public static List<Protocol> getCluster() {
        /*
         * start three application with different config to simulate cluster
         * then they will elect for leader
         */
        return Stream.of(new RaftProtocol("example-server0.properties"),
                        new RaftProtocol("example-server1.properties"),
                        new RaftProtocol("example-server2.properties"))
                .collect(Collectors.toList());
    }
}
