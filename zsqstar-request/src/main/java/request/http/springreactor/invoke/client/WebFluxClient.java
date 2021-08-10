package request.http.springreactor.invoke.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/1/14 3:14 下午
 *  @Description: 响应式服务间调用注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebFluxClient {

    /**
     * 注册的应用名，有注册中心的时候
     * @return
     */
    String value() default "";
}
