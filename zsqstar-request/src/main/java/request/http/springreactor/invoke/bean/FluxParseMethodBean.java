package request.http.springreactor.invoke.bean;

import lombok.Data;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/1/21 10:00 上午
 *  @Description: 解析类里面方法的传参
 *  url, 返回值等信息
 */
@Data
public class FluxParseMethodBean {

    /**
     * 映射地址
     */
    private String url;

    /**
     * http请求类型
     */
    private HttpMethod requestMethod;

    /**
     * 请求url上的参数
     */
    private Map<String,Object> params;


    /**
     * RequestBody数据
     */
    private Mono<?> body;


    /**
     * body类型
     */
    private Class<?> bodyType;

    /**
     * 是否返回flux
     */
    private Boolean returnFlux = false;

    /**
     * 返回值类型
     */
    private Class<?> returnElementType;
}

