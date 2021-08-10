package request.http.springreactor.invoke.handler;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import request.http.springreactor.invoke.bean.FluxParseClassBean;
import request.http.springreactor.invoke.bean.FluxParseMethodBean;
import request.http.springreactor.invoke.client.WebFluxClient;
import request.http.springreactor.invoke.rest.RestHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/1/25 10:23 上午
 * @Description: 动态代理执行
 */
public class DynamicProxyBeanFactory<T> implements InvocationHandler {

    private Class<T> interfaceType;

    private RestHandler restHandler;

    public DynamicProxyBeanFactory(Class<T> interfaceType, RestHandler restHandler) {
        this.interfaceType = interfaceType;
        this.restHandler = restHandler;
    }

    /**
     * 代理执行main
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //1. 解析类
        FluxParseClassBean fluxParseClassBean = parseClass(method);
        //初始化调用对象
        restHandler.init(fluxParseClassBean);

        //2. 解析方法
        FluxParseMethodBean fluxParseMethodBean = parseMethod(method, args);
        return restHandler.invokeRest(fluxParseMethodBean);
    }

    /**
     * webflux解析类
     *
     * @param method
     * @return
     */
    private FluxParseClassBean parseClass(Method method) {
        FluxParseClassBean fluxParseClassBean = new FluxParseClassBean();
        RequestMapping classRequestMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
        if (classRequestMapping != null) {
            if (classRequestMapping.value() != null && classRequestMapping.value().length > 0) {
                fluxParseClassBean.setUrlPrefix(classRequestMapping.value()[0]);
            }
        }
        //解析注解上的请求地址
        WebFluxClient webFluxClient = method.getDeclaringClass().getAnnotation(WebFluxClient.class);
        fluxParseClassBean.setServiceUrl(webFluxClient.value());
        return fluxParseClassBean;
    }

    /**
     * 解析放在对象里
     *
     * @param method
     * @param args
     * @return
     */
    private FluxParseMethodBean parseMethod(Method method, Object[] args) {
        FluxParseMethodBean fluxParseMethodBean = new FluxParseMethodBean();

        /**解析url和url上的请求参数*/
        parseUrlAndMethod(method, fluxParseMethodBean);

        /**解析参数和请求体*/
        parseParamAndBody(method, args, fluxParseMethodBean);

        /**解析返回值和类型*/
        parseReturnType(method, fluxParseMethodBean);

        return fluxParseMethodBean;
    }


    /**
     * 解析参数和请求体
     *
     * @param method
     * @param fluxParseMethodBean
     */
    private void parseParamAndBody(Method method, Object[] args, FluxParseMethodBean fluxParseMethodBean) {
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length > 0) {
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                //是否带了@PathVariable注解
                PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                if (pathVariable != null) {
                    params.put(pathVariable.value(), args[i]);
                }

                //是否带了@RequestBody注解
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                if (requestBody != null) {
                    fluxParseMethodBean.setBody((Mono<?>) args[i]);
                    Type[] types = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
                    if (types != null && types.length > 0) {
                        fluxParseMethodBean.setBodyType((Class<?>) types[0]);
                    }
                }

            }
            fluxParseMethodBean.setParams(params);
        }
    }

    /**
     * 解析返回值类型
     *
     * @param method
     * @param fluxParseMethodBean
     */
    private void parseReturnType(Method method, FluxParseMethodBean fluxParseMethodBean) {
        Class<?> returnType = method.getReturnType();
        if (returnType.isAssignableFrom(Flux.class)) {
            fluxParseMethodBean.setReturnFlux(true);
        }
        Type[] types = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
        if (types != null && types.length > 0) {
            fluxParseMethodBean.setReturnElementType((Class<?>) types[0]);
        }
    }

    /**
     * 解析方法的url和请求方式
     *
     * @param method
     * @param fluxParseMethodBean
     */
    private void parseUrlAndMethod(Method method, FluxParseMethodBean fluxParseMethodBean) {
        String REQUEST_METHOD = null;
        String methodUrl = "";
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation instanceof RequestMapping) {
                RequestMapping methodRequestMapping = (RequestMapping) annotation;
                if (methodRequestMapping.value() != null && methodRequestMapping.value().length > 0) {
                    methodUrl = methodRequestMapping.value()[0];
                }
                if (methodRequestMapping.method() != null && methodRequestMapping.method().length > 0) {
                    RequestMethod requestMethod = methodRequestMapping.method()[0];
                    REQUEST_METHOD = requestMethod.name();
                }
                fluxParseMethodBean.setUrl(methodUrl);
                fluxParseMethodBean.setRequestMethod(HttpMethod.valueOf(REQUEST_METHOD));
            }

            if (annotation instanceof GetMapping) {
                GetMapping methodRequestMapping = (GetMapping) annotation;
                if (methodRequestMapping.value() != null && methodRequestMapping.value().length > 0) {
                    methodUrl = methodRequestMapping.value()[0];
                }
                if (methodRequestMapping.path() != null && methodRequestMapping.path().length > 0) {
                    methodUrl = methodRequestMapping.path()[0];
                }
                REQUEST_METHOD = RequestMethod.GET.name();
                fluxParseMethodBean.setUrl(methodUrl);
                fluxParseMethodBean.setRequestMethod(HttpMethod.GET);
            }

            if (annotation instanceof PostMapping) {
                PostMapping methodRequestMapping = (PostMapping) annotation;
                if (methodRequestMapping.value() != null && methodRequestMapping.value().length > 0) {
                    methodUrl = methodRequestMapping.value()[0];
                }
                REQUEST_METHOD = RequestMethod.POST.name();
                fluxParseMethodBean.setUrl(methodUrl);
                fluxParseMethodBean.setRequestMethod(HttpMethod.POST);
            }

            if (annotation instanceof PutMapping) {
                PutMapping methodRequestMapping = (PutMapping) annotation;
                if (methodRequestMapping.value() != null && methodRequestMapping.value().length > 0) {
                    methodUrl = methodRequestMapping.value()[0];
                }
                REQUEST_METHOD = RequestMethod.PUT.name();
                fluxParseMethodBean.setUrl(methodUrl);
                fluxParseMethodBean.setRequestMethod(HttpMethod.PUT);
            }

            if (annotation instanceof DeleteMapping) {
                DeleteMapping methodRequestMapping = (DeleteMapping) annotation;
                if (methodRequestMapping.value() != null && methodRequestMapping.value().length > 0) {
                    methodUrl = methodRequestMapping.value()[0];
                }
                if (methodRequestMapping.path() != null && methodRequestMapping.path().length > 0) {
                    methodUrl = methodRequestMapping.path()[0];
                }
                REQUEST_METHOD = RequestMethod.DELETE.name();
                fluxParseMethodBean.setUrl(methodUrl);
                fluxParseMethodBean.setRequestMethod(HttpMethod.DELETE);
            }
        }
    }


}
