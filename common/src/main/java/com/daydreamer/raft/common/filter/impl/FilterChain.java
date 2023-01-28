package com.daydreamer.raft.common.filter.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.filter.LogFilter;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;

import java.util.ArrayList;
import java.util.List;

@SPIImplement("filterChain")
public class FilterChain implements LogFilter, GroupAware {

    private final List<LogFilter> filters = new ArrayList<>();

    private String groupKey;

    @SPIMethodInit
    private void init() {
        List<LogFilter> all = RaftServiceLoader.getLoader(groupKey, LogFilter.class).getAll();
        if (all.size() > 1) {
            filters.addAll(all);
            filters.remove(this);
        }
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }

    @Override
    public boolean filter(LogEntry logEntry) {
        for (LogFilter filter : filters) {
            // if anyone return false, then reject
            if (!filter.filter(logEntry)) {
                return false;
            }
        }
        return true;
    }
}
