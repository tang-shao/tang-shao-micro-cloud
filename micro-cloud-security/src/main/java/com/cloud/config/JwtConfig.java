package com.cloud.config;


public class JwtConfig {

    /**
     * Spring Security 常量
     */
    // Request Headers
    public static final String HEADER = "X-Access-Token";

    // token，最后留个空格 Bearer
    public static final String TOKENSTARTWITH = "Bearer";

    // 必须使用最少88位的Base64对该token进行编码
    public static final String BASE64SECRET = "ZmQ0ZGI5NjQ0MDQwY2I4MjMxY2Y3ZmI3MjdhN2ZmMjNhODViOTg1ZGE0NTBjMGM4NDA5NzYxMjdjOWMwYWRmZTBlZjlhNGY3ZTg4Y2U3YTE1ODVkZDU5Y2Y3OGYwZWE1NzUzNWQ2YjFjZDc0NGMxZWU2MmQ3MjY1NzJmNTE0MzI=";

    // JWT加密秘钥
    public static final String SECRET = "micro-cloud-secret";

    // token过期时间 半小时
    public static final Long TOKENVALIDITYINSECONDS = 1800000L;

    // 在线用户 key，根据 key 查询 redis 中在线用户的数据
    public static final String ONLINEKEY = "online-token-";

    // 验证码
    public static final String CODEKEY = "code-key-";

    // token 续期检查( 在token即将过期的一段时间内用户操作了，则给用户的token续期 )
    public static final Long DETECT = 1800000L;

    // 续期时间范围，默认1小时，单位毫秒
    public static final Long RENEW = 3600000L;

    public static String getTokenStartWith() {
        return TOKENSTARTWITH + " ";
    }

}
