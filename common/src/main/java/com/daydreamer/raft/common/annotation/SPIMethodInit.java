package com.daydreamer.raft.common.annotation;


import java.lang.annotation.*;

/**
 * @author Daydreamer
 * <p>
 * Invoke after properties set
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SPIMethodInit {
}
