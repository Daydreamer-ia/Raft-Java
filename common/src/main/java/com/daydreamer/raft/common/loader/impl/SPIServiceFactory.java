package com.daydreamer.raft.common.loader.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.common.loader.ServiceFactory;

/**
 * @author Daydreamer
 */
@SPIImplement("spiServiceFactory")
public class SPIServiceFactory implements ServiceFactory {

    @Override
    public <T> T getDependency(Class<T> type, String name) {
        return RaftServiceLoader.getLoader(type).getInstance(name);
    }
}
