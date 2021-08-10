package request.http.springreactor.invoke.register;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import request.http.springreactor.invoke.annotation.EnableWebfluxClientInvoke;

import java.util.Map;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/1/21 5:52 下午
 * @Description: 扫描api的包注册到
 * map中
 */
@Slf4j
public class WebfluxImportBeanDefinitionRegister implements ImportBeanDefinitionRegistrar {

    private static String componentScan;

    private final static String COMPONENT_SCAN = "componentScan";


    /**
     * 获取EnableWebfluxClientInvoke注解值, 放入到类
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributesMap = importingClassMetadata.
                getAnnotationAttributes(EnableWebfluxClientInvoke.class.getName());
        String componentScan = (String) annotationAttributesMap.get(COMPONENT_SCAN);

        log.info("webflux包扫描："+componentScan);
        WebfluxImportBeanDefinitionRegister.componentScan = componentScan;
    }

    /**
     * 获取类中的扫包路径
     * @return
     */
    public static String getComponentScan() {
        return componentScan;
    }

    public static void setComponentScan(String componentScan) {
        WebfluxImportBeanDefinitionRegister.componentScan = componentScan;
    }
}
