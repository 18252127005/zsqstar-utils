package request.http.springreactor.invoke.annotation;

import org.springframework.context.annotation.Import;
import request.http.springreactor.invoke.register.WebfluxImportBeanDefinitionRegister;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/1/22 2:04 下午
 *  @Description: springboot启动类注解, 扫包有FluxClient的注解
 *  主入口
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WebfluxImportBeanDefinitionRegister.class)
public @interface EnableWebfluxClientInvoke {
    /**
     * 包扫描路径，扫描@WebFluxClient
     * @return
     */
    String componentScan()  default "";
}
