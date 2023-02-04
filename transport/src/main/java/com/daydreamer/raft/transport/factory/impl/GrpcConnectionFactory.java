package com.daydreamer.raft.transport.factory.impl;

import com.daydreamer.raft.api.grpc.RequesterGrpc;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcConnection;
import com.daydreamer.raft.transport.factory.ConnectionFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Map;

/**
 * @author Daydreamer
 */
@SPIImplement("grpc")
public class GrpcConnectionFactory implements ConnectionFactory {

    private static final String SEPARATOR = ":";

    @Override
    public Connection getConnection(String ip, Integer port, Map<Object, Object> metadata) {
        // init channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
        // init service rpc Stub
        RequesterGrpc.RequesterBlockingStub blockingStub = RequesterGrpc.newBlockingStub(channel);
        return new GrpcConnection(ip + SEPARATOR + port, blockingStub);
    }

}
