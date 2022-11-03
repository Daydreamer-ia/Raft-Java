package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcRequestServerCore;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author Daydreamer
 */
public class GrpcRaftServer extends AbstractRaftServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcRaftServer.class);
    
    /**
     * server
     */
    private Server server;
    
    /**
     * raft member manager
     */
    private RaftMemberManager raftMemberManager;
    
    public GrpcRaftServer(RaftConfig raftConfig, RaftMemberManager raftMemberManager) {
        super(raftConfig);
        this.raftMemberManager = raftMemberManager;
    }
    
    @Override
    protected void doStartServer() {
        try {
            int port = raftConfig.getPort();
            server = ServerBuilder.forPort(port)
                    .addService(new GrpcRequestServerCore(new GrpcConnectionManager(raftMemberManager, raftConfig)))
                    .build()
                    .start();
            LOGGER.trace("[GrpcRaftServer] - Server started, listening on port: " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                LOGGER.trace("[GrpcRaftServer] - shutting down gRPC server since JVM is shutting down...");
                this.close();
                LOGGER.trace("[GrpcRaftServer] - server shut down");
            }));
        } catch (Exception e) {
            throw new IllegalStateException("[GrpcRaftServer] - Fail to init server, because " + e.getLocalizedMessage());
        }
    }
    
    @Override
    public boolean requestVote() {
        return false;
    }
    
    @Override
    public boolean isLeader() {
        return false;
    }
    
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
    
    @Override
    public void close() {
       try {
           if (server != null) {
               server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
           }
       } catch (Exception e) {
           throw new IllegalStateException("[GrpcRaftServer] - Fail to close server, because " + e.getLocalizedMessage());
       }
    }
}
