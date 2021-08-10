package request.http.springreactor.invoke.bean;

import lombok.Data;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/1/21 9:57 上午
 *  @Description: FLux解析class上的client实体
 *  内容
 */
@Data
public class FluxParseClassBean {

    /**
     * 服务的请求地址 具体调用的远程地址 http://ip:port/
     */
    private String serviceUrl;

    /**
     * 类上的地址前缀 即类上的@RequestMapping
     */
    private String urlPrefix;
}
