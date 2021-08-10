package request.http.springreactor.invoke.rest;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import request.http.springreactor.invoke.bean.FluxParseClassBean;
import request.http.springreactor.invoke.bean.FluxParseMethodBean;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/1/22 2:32 下午
 * @Description: command
 * class and methods
 */
@Slf4j
public class WebClientRestHandler implements RestHandler {
    private WebClient client;
    private FluxParseClassBean fluxParseClassBean;


    @Override
    public void init(FluxParseClassBean fluxParseClassBean) {
        this.fluxParseClassBean = fluxParseClassBean;
        this.client = WebClient.create(fluxParseClassBean.getServiceUrl());
    }

    @Override
    public Object invokeRest(FluxParseMethodBean fluxParseMethodBean) {
        log.info("调用前参数fluxParseMethodBean：{}", JSON.toJSONString(fluxParseMethodBean));
        Object result = null;

        // 拼接url
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(fluxParseClassBean.getUrlPrefix())) {
            sb.append(fluxParseClassBean.getUrlPrefix());
        }

        if (StringUtils.isNotBlank(fluxParseMethodBean.getUrl())) {
            sb.append(fluxParseMethodBean.getUrl());
        }
        //这里可以得到参数数组和方法等，可以通过反射，注解等，进行结果集的处理
        WebClient.RequestBodyUriSpec requestBodyUriSpec = this.client
                //请求方法
                .method(fluxParseMethodBean.getRequestMethod());


        //请求地址
        WebClient.RequestBodySpec responseBodySpec = null;
        if (fluxParseMethodBean.getParams() != null) {
            responseBodySpec = requestBodyUriSpec.uri(sb.toString(), fluxParseMethodBean.getParams());
        } else {
            responseBodySpec = requestBodyUriSpec.uri(sb.toString());
        }


        //接收格式
        WebClient.ResponseSpec responseSpec = null;
        if (fluxParseMethodBean.getBody() != null) {
            responseSpec = responseBodySpec.body(fluxParseMethodBean.getBody(), fluxParseMethodBean.getBodyType())
                    .accept(MediaType.APPLICATION_JSON)
                    //发出请求
                    .retrieve();
        } else {
            responseSpec = responseBodySpec.accept(MediaType.APPLICATION_JSON)
                    //发出请求
                    .retrieve();
        }


        //处理请求
        if(fluxParseMethodBean.getReturnFlux()){
            result = responseSpec.bodyToFlux(fluxParseMethodBean.getReturnElementType());
        }else {
            result = responseSpec.bodyToMono(fluxParseMethodBean.getReturnElementType());
        }


        return result;
    }
}
