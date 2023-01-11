package com.daydreamer.raft.common.loader.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.common.loader.ServiceFactory;

import java.lang.reflect.Modifier;

/**
 * @author Daydreamer
 */
@SPIImplement("spiServiceFactory")
public class SPIServiceFactory implements ServiceFactory {

    private String groupKey;

    @Override
    public <T> T getDependency(Class<T> type, String name) {
        // if interface or abstract class, then try to find
        if (Modifier.isInterface(type.getModifiers())
                || Modifier.isAbstract(type.getModifiers())) {
            return RaftServiceLoader.getLoader(groupKey, type).getInstance(name);
        }
        // no found
        return null;
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
