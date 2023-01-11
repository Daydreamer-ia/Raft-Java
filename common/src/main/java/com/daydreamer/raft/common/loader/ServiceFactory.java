package com.daydreamer.raft.common.loader;

import com.daydreamer.raft.common.annotation.SPI;

/**
 * @author Daydreamer
 */
@SPI("adaptiveServiceFactory")
public interface ServiceFactory extends GroupAware{

    <T> T getDependency(Class<T> type, String name);
}
