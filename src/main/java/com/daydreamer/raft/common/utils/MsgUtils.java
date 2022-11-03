package com.daydreamer.raft.common.utils;

import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.grpc.Message;
import com.google.gson.Gson;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daydreamer
 */
public class MsgUtils {
    
    private static final String REQUEST_PACKAGE = "com/daydreamer/raft/transport/entity/request";
    
    private static final String RESPONSE_PACKAGE = "com/daydreamer/raft/transport/entity/response";
    
    private static final String CLASSPATH_PREFIX = "src/main/java/";
    
    private static final String PACKAGE_SEPARATOR = ".";
    
    private static final Map<String, Class<?>> ENTITY_MAP = new ConcurrentHashMap<>();
    
    static {
        // load request
        load(CLASSPATH_PREFIX + REQUEST_PACKAGE);
        // load response
        load(CLASSPATH_PREFIX + RESPONSE_PACKAGE);
    }
    
    /**
     * load entity type from package
     *
     * @param path base package
     */
    public static void load(String path) {
        try {
            // load instance
            String packagePrefx = path.replaceAll(CLASSPATH_PREFIX, "").replaceAll("/", ".");
            File file = new File(path);
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    String clazzName = packagePrefx + PACKAGE_SEPARATOR + child.getName().replace(".java", "");
                    Class<?> clazz = Class.forName(clazzName);
                    ENTITY_MAP.put(clazzName, clazz);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can not load base entity, because ", e);
        }
    }
    
    public static Request convertRequest(Message message) {
        String type = message.getType();
        Class<?> targetClazz = ENTITY_MAP.get(type);
        return (Request) convert(message, targetClazz);
    }
    
    public static Response convertResponse(Message message) {
        String type = message.getType();
        Class<?> targetClazz = ENTITY_MAP.get(type);
        return (Response) convert(message, targetClazz);
    }
    
    private static Object convert(Message message, Class<?> targetClazz) {
        String data = message.getData();
        try {
            Gson gson = new Gson();
            return gson.fromJson(data, targetClazz);
        }catch (Exception e) {
            throw new IllegalArgumentException("Cannot covert instance, because " + e.getLocalizedMessage());
        }
    }
    
    public static Message convertMsg(Serializable serializable) {
        Gson gson = new Gson();
        String data = gson.toJson(serializable);
        String type = serializable.getClass().getName();
        return Message.newBuilder().setData(data).setType(type).build();
    }
    
}
