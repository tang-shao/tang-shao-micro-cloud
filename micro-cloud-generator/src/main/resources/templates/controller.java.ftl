package ${package.Controller};


import org.springframework.web.bind.annotation.RequestMapping;

<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import com.cloud.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.cloud.common.api.Result;
import ${package.Service}.${table.serviceName};
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import ${package.Entity}.${entity};
import javax.validation.Valid;


/**
 * ${table.comment!} 前端控制器
 *
 * @author ${author}
 * @since ${date}
 */
@Slf4j
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
@Api(tags="${table.comment!}管理")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>

    @Autowired
    private ${table.serviceName} ${(table.serviceName?substring(0))?uncap_first};

    /**
    *   新增
    */
    @PostMapping(value = "/add")
    @AutoLog(value = "${table.comment!}-添加")
    @ApiOperation(value="${table.comment!}-添加", notes="${table.comment!}-添加")
    public Result<?> edit(@RequestBody @Valid ${entity} ${entity?uncap_first}){
        ${table.serviceName?uncap_first}.save(${entity?uncap_first});
        return Result.ok("添加成功!");
    }


    /**
    *   编辑
    */
    @PostMapping(value = "/edit")
    @AutoLog(value = "${table.comment!}-编辑")
    @ApiOperation(value="${table.comment!}-编辑", notes="${table.comment!}-编辑")
    public Result<?> add(@RequestBody @Valid ${entity} ${entity?uncap_first}){
        ${table.serviceName?uncap_first}.updateById(${entity?uncap_first});
        return Result.ok("编辑成功!");
    }

    /**
    *   通过ID删除
    */
    @DeleteMapping(value = "/delete")
    @AutoLog(value = "${table.comment!}-用过ID删除")
    @ApiOperation(value="${table.comment!}-通过ID删除", notes="${table.comment!}-通过ID删除")
    public Result<?> delete(@RequestParam(name="id",required=true) String id){
        ${table.serviceName?uncap_first}.removeById(id);
        return Result.ok("删除成功!");
    }

    /**
    *   批量删除
    */
    @DeleteMapping(value = "/deleteBatch")
    @AutoLog(value = "${table.comment!}-根据ID批量删除")
    @ApiOperation(value="${table.comment!}-根据ID批量删除", notes="${table.comment!}-根据ID批量删除")
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids){
        this.${table.serviceName?uncap_first}.removeByIds(Arrays.asList(ids.split(",")))
    }

    /**
    *   通过id查询
    */
    @GetMapping(value = "/queryById")
    @AutoLog(value = "${table.comment!}-通过id查询")
    @ApiOperation(value="${table.comment!}-通过id查询", notes="${table.comment!}-通过id查询")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id){
        ${entity} ${entity?uncap_first} = ${table.serviceName?uncap_first}.getById(id);
        return Result.ok(${entity?uncap_first});
    }


}
</#if>
