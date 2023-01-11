package com.daydreamer.raft.common.annotation;

import java.lang.annotation.*;

/**
 * @author Daydreamer
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPIImplement {

    /**
     * key of current implement
     *
     * @return key
     */
    String value();

}
