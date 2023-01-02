package com.daydreamer.raft.common.loader;

import com.daydreamer.raft.common.annotation.SPI;

/**
 * @author Daydreamer
 */
@SPI("adaptiveServiceFactory")
public interface ServiceFactory {

    <T> T getDependency(Class<T> type, String name);
}
