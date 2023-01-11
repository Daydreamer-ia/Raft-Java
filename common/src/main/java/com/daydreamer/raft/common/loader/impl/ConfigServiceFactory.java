package com.daydreamer.raft.common.loader.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.loader.ServiceFactory;
import com.daydreamer.raft.common.service.ActiveProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daydreamer
 */
@SPIImplement("configServiceFactory")
public class ConfigServiceFactory implements ServiceFactory {

    private final Map<String, ActiveProperties> map = new ConcurrentHashMap<>();

    private String groupKey;

    @Override
    public <T> T getDependency(Class<T> type, String name) {
        if (map.containsKey(name) && map.get(name).getClass().equals(type)) {
            return type.cast(map.get(name));
        }
        return null;
    }

    /**
     * add property
     *
     * @param name       property name
     * @param properties property
     */
    public void addProperty(String name, ActiveProperties properties) {
        map.putIfAbsent(name, properties);
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
