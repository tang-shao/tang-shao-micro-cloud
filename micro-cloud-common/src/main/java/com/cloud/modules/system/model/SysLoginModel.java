package com.cloud.modules.system.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(value = "登录对象",description = "登录对象")
public class SysLoginModel implements Serializable {

    /**
     * @NotNull：不能为null，但可以为empty  一般用在基本类型上
     * @NotEmpty：不能为null，而且长度必须大于0  一般使用在集合上面
     * @NotBlank：只能作用在String上，不能为null，而且调用trim()后，长度必须大于0 即：必须有实际字符
     */

    @ApiModelProperty("账号")
    @NotBlank(message = "账号不能是空")
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能是空")
    private String password;

    @ApiModelProperty("验证码")
    @NotBlank(message = "验证码不能是空")
    private String captcha;

    @ApiModelProperty("验证码唯一标识")
    private String uuid = "";

    @Override
    public String toString() {
        return "{username=" + username  + ", password= " + password +"}";
    }


}
