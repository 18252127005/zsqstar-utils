package request.http.springreactor.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import request.http.springreactor.invoke.client.WebFluxClient;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/1/14 3:29 下午
 *  @Description: 案列请求
 */
@WebFluxClient(value = "http://localhost:8080/")
@RequestMapping("/demo")
public interface DemoApi {

    /**
     * 查询所有
     * @return
     */
    @GetMapping
    Flux<JSONObject> findAll();

    /**
     * 根据id查询详情
     * @param id
     * @return
     */
    @GetMapping (path = "/{id}")
    Mono<JSONObject> findById(@PathVariable("id") long id);

    /**
     * 保存单个
     * @param employee
     * @return
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<String> save(@RequestBody Mono<JSONObject> employee);

    /**
     * 修改单个
     * @param employee
     * @return
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> update(@RequestBody Mono<JSONObject> employee);

    /**
     * 根据条件删除
     * @param employee
     * @return
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> delete(@RequestBody Mono<JSONObject> employee);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> deleteById(@PathVariable("id") long id);
}
