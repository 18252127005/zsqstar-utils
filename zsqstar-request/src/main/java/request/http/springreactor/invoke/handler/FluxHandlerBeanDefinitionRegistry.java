package request.http.springreactor.invoke.handler;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import request.http.springreactor.invoke.client.WebFluxClient;
import request.http.springreactor.invoke.register.WebfluxImportBeanDefinitionRegister;

import java.util.Set;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/1/25 10:42 上午
 * @Description: (@ WebFluxClient 注解扫描并解析生成代理类注册到spring容器)
 * * 1.实现BeanDefinitionRegistryPostProcessor，扫描到特定注解的类
 * * 2.实现FactoryBean，为添加注解的接口类生成代理对象，getObject的时候返回代理对象
 * * 3.将FactoryBean设置到GenericBeanDefinition
 * * 4.自动注入时，调用接口方法执行代理对象的方法
 */
@Slf4j
@Component
public class FluxHandlerBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final String INTERFACE_CLASS = "interfaceClass";

    /**
     * 获取@WebFluxClient注解的接口，这些接口就需要通过动态代理提供默认实现
     *
     * @param beanDefinitionRegistry
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Set<Class<?>> classes = getAutoImplClasses();
        for (Class<?> clazz : classes) {
            // 生成GenericBeanDefinition 并注入到容器
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
            definition.getPropertyValues().add(INTERFACE_CLASS, clazz);

            //设置factoryBean 用于生成代理类
            definition.setBeanClass(HandlerInterfaceFactoryBean.class);
            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

            //注册bean
            beanDefinitionRegistry.registerBeanDefinition(decapitalize(clazz.getSimpleName()), definition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        log.info("------------------------>postProcessBeanFactory");
    }

    /**
     * 通过反射扫描出所有使用HandlerRouterAutoImpl的类
     *
     * @return
     */
    private Set<Class<?>> getAutoImplClasses() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                // 指定路径URL
                .forPackages(WebfluxImportBeanDefinitionRegister.getComponentScan())
                // 添加子类扫描工具
                .addScanners(new SubTypesScanner())
                // 添加 属性注解扫描工具
                .addScanners(new FieldAnnotationsScanner())
                // 添加 方法注解扫描工具
                .addScanners(new MethodAnnotationsScanner())
                // 添加方法参数扫描工具
                .addScanners(new MethodParameterScanner())
        );
        return reflections.getTypesAnnotatedWith(WebFluxClient.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("------------------->setApplicationContext");
    }


    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
