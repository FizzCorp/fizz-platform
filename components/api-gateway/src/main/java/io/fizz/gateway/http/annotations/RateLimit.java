package io.fizz.gateway.http.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {
    RLScope scope() default RLScope.NONE;
    RLKeyType type() default RLKeyType.PATH;
    String keyName() default "";
}