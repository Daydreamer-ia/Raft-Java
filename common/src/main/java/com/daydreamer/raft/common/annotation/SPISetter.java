package com.daydreamer.raft.common.annotation;

import java.lang.annotation.*;

/**
 * @author Daydreamer
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SPISetter {

    /**
     * dependency key
     *
     * @return key
     */
    String value();
}
