package request.server.window.pojo;

import lombok.Data;

/**
 *  @author: Mark.ZSQ
 *  @Date: 2021/2/4 3:28 下午
 *  @Description:
 */
@Data
public class User {
    private String userName;
    private String userPwd;
    private String ip;
    private Integer port;
}
