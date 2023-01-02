package com.daydreamer.raft.common.entity;

/**
 * @author Daydreamer
 */
public class Holder {

    private Object object;

    private Class<?> clazz;

    public Holder() {
    }

    public Holder(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Holder(Object object, Class<?> clazz) {
        this.object = object;
        this.clazz = clazz;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
}
