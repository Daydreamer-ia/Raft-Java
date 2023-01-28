package com.daydreamer.raft.protocol.filter;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.common.filter.LogFilter;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.constant.LogConstant;

import java.util.Map;

/**
 * @author Daydreamer
 */
@SPIImplement("innerLogFilter")
public class InnerLogFilter implements LogFilter {

    @Override
    public boolean filter(LogEntry logEntry) {
        Payload payload = logEntry.getPayload();
        if (payload == null) {
            return false;
        }
        Map<String, String> metadata = payload.getMetadata();
        if (metadata == null) {
            return false;
        }
        String val = metadata.get(LogConstant.INNER_LOG_TAG);
        return val != null;
    }
}
