package com.cloud.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具类
 */
public class GatewayUtils {

    /**
     * redis reload 事件
     */
    public static final String ROUTE_REDIS_RELOAD_TOPIC = "gateway_redis_route_reload_topic";

    /**
     * 内存reload 时间
     */
    public static final String ROUTE_JVM_RELOAD_TOPIC = "gateway_jvm_route_reload_topic";


    /**
     *  redis中的信息需要处理下 转成RouteDefinition对象
     *         - id: login
     *           uri: lb://cloud-jeecg-system
     *           predicates:
     *             - Path=/jeecg-boot/sys/**,
     * @param array
     * @return
     */

    public static List<RouteDefinition> getRoutesByJson(JSONArray array) throws URISyntaxException {
        List<RouteDefinition> ls = new ArrayList<>();
        for(int i=0;i<array.size();i++) {
            JSONObject obj = array.getJSONObject(i);
            RouteDefinition route = new RouteDefinition();
            route.setId(obj.getString("id"));
            Object uri = obj.get("uri");
            if(uri==null){
                route.setUri(new URI("lb://"+obj.getString("name")));
            }else{
                route.setUri(new URI(obj.getString("uri")));
            }
            Object predicates = obj.get("predicates");
            if (predicates != null) {
                JSONArray predicatesArray = JSONArray.parseArray(predicates.toString());
                List<PredicateDefinition> predicateDefinitionList =
                        predicatesArray.toJavaList(PredicateDefinition.class);
                route.setPredicates(predicateDefinitionList);
            }

            Object filters = obj.get("filters");
            if (filters != null) {
                JSONArray filtersArray = JSONArray.parseArray(filters.toString());
                List<FilterDefinition> filterDefinitionList
                        = filtersArray.toJavaList(FilterDefinition.class);
                route.setFilters(filterDefinitionList);
            }
            ls.add(route);
        }
        return ls;
    }
}
