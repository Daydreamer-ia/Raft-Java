package com.daydreamer.raft.common.loader.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.common.loader.ServiceFactory;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daydreamer
 */
@SPIImplement("adaptiveServiceFactory")
public class AdaptiveServiceFactory implements ServiceFactory {

    private List<ServiceFactory> serviceFactories;

    public AdaptiveServiceFactory() {
        RaftServiceLoader<ServiceFactory> loader = RaftServiceLoader.getLoader(ServiceFactory.class);
        serviceFactories = loader.getAll();
        // remove current
        serviceFactories.remove(this);
    }

    @Override
    public <T> T getDependency(Class<T> type, String name) {
        for (ServiceFactory serviceFactory : serviceFactories) {
            T dependency = serviceFactory.getDependency(type, name);
            if (dependency != null) {
                return dependency;
            }
        }
        return null;
    }
}
