package com.daydreamer.raft.transport.factory;

import com.daydreamer.raft.common.annotation.SPI;
import com.daydreamer.raft.transport.connection.Connection;

import java.util.Map;

/**
 * @author Daydreamer
 * <p>
 * Factory to get connection
 */
@SPI("grpc")
public interface ConnectionFactory {

    /**
     * get connection
     *
     * @return get connection
     */
    Connection getConnection(String ip, Integer port, Map<Object, Object> metadata);
}
