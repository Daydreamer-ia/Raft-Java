package com.daydreamer.raft.common.utils;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.grpc.Message;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daydreamer
 */
public class MsgUtils {
    
    private static final String REQUEST_PACKAGE = "com/daydreamer/raft/api/entity/request/";
    
    private static final String RESPONSE_PACKAGE = "com/daydreamer/raft/api/entity/response/";
    
    private static final String CLASS_FORMAT = ".class";
    
    private static final String FILE_SEPARATOR = "/";
    
    private static final String EMPTY = "";
    
    private static final String PACKAGE_SEPARATOR = ".";
    
    private static final Map<String, Class<?>> ENTITY_MAP = new ConcurrentHashMap<>();
    
    static {
        // load request
        load(REQUEST_PACKAGE);
        // load response
        load(RESPONSE_PACKAGE);
    }
    
    /**
     * load entity type from package
     *
     * @param path base package
     */
    public static void load(String path) {
        try {
            String classPre = path.replaceAll(FILE_SEPARATOR, PACKAGE_SEPARATOR);
            // load instance
            ClassLoader classLoader = MsgUtils.class.getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(path)).getFile());
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    String clazzName = classPre + child.getName().replace(CLASS_FORMAT, EMPTY);
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
        } catch (Exception e) {
            e.printStackTrace();
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
