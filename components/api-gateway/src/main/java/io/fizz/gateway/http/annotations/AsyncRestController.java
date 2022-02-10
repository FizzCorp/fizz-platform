package io.fizz.gateway.http.annotations;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AsyncRestController {
    String path() default "";
    HttpMethod method() default HttpMethod.GET;
    AuthScheme auth() default AuthScheme.NONE;
}
