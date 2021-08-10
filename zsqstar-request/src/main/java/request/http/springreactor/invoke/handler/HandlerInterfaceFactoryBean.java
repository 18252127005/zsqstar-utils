package request.http.springreactor.invoke.handler;

import org.springframework.beans.factory.FactoryBean;
import request.http.springreactor.invoke.rest.WebClientRestHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/1/25 10:21 上午
 * @Description: 生成代理对象走代理
 */
public class HandlerInterfaceFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceClass;

    @Override
    public T getObject() throws Exception {
        //生成远程调用对象
        WebClientRestHandler webClientRestHandler = new WebClientRestHandler();
        //生成调用处理器
        InvocationHandler handler = new DynamicProxyBeanFactory(interfaceClass, webClientRestHandler);
        //返回代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
