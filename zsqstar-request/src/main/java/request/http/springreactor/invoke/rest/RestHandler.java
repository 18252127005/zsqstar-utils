package request.http.springreactor.invoke.rest;


import request.http.springreactor.invoke.bean.FluxParseClassBean;
import request.http.springreactor.invoke.bean.FluxParseMethodBean;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/1/22 2:23 下午
 *  @Description: RESTFUL请求方式进行执行
 */
public interface RestHandler {

    /**
     * 初始化 api的类
     * @param fluxParseClassBean
     * @return
     */
    void init(FluxParseClassBean fluxParseClassBean);

    /**
     * 反射调用api类上的方法
     * @param fluxParseMethodBean
     * @return
     */
    Object invokeRest(FluxParseMethodBean fluxParseMethodBean);
}
