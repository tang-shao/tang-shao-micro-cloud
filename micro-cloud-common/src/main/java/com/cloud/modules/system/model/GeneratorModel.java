package com.cloud.modules.system.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "代码生成器对象",description = "代码生成器对象")
public class GeneratorModel {

    @ApiModelProperty("模块名称")
    private String modelName;

    @ApiModelProperty("项目名称,代码生成在哪个项目下面")
    private String servicesName;

    @ApiModelProperty("表名,有多张请用英文逗号分割")
    private String tableName;

    @ApiModelProperty("作者")
    private String author;



}
