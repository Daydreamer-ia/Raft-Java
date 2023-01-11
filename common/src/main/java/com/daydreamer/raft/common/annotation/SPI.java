package com.daydreamer.raft.common.annotation;

import java.lang.annotation.*;

/**
 * @author Daydreamer
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * default implement key
     *
     * @return default implement key
     */
    String value();
}
