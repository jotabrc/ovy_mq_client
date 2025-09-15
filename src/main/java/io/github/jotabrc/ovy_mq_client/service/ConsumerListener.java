package io.github.jotabrc.ovy_mq_client.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConsumerListener {
    String value() default "";
    Class<?> payloadType();
}
