package com.hongdeyan.annotation;

import java.lang.annotation.*;

/**
 * id标注注解.用于自己的orm框架识别那个属性是id主键
 *
 * @author hdy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
    public String param_name() default "";
}
