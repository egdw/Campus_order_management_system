package com.hongdeyan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Document
@Target(ElementType.TYPE)
public @interface Document {
    String doucument_name() default "";
}
