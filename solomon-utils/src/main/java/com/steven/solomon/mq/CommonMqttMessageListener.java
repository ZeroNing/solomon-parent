package com.steven.solomon.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.pojo.entity.BaseMq;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public interface CommonMqttMessageListener<T,R,M extends BaseMq<T>> {

    /**
     * 消费方法
     */
    R handleMessage(T body) throws Exception;

    /**
     * 保存消费成功/失败的消息
     */
    void saveLog(R result, Throwable throwable, M model);

    /**
     * 判断是否重复消费
     * @return true 重复消费 false 不重复消费
     */
    default boolean checkMessageKey(M model){
        return false;
    }
    /**
     * 删除判断重复消费Key
     */
    default void deleteCheckMessageKey(M model){}

    /**
     * 转换消息
     */
    default M conversion(String json){
        Map<Type, Type> typeMap = TypeUtil.getTypeMap(getClass());
        ParameterizedType parameterizedType = null;
        for(Map.Entry<Type,Type> entry: typeMap.entrySet()){
            if(StrUtil.equalsAnyIgnoreCase("M",entry.getKey().getTypeName())){
                parameterizedType = (ParameterizedType) entry.getValue();
                break;
            }
        }
        M model = JSONUtil.toBean(json, parameterizedType,true);
        T body = model.getBody();
        boolean isJsonObject = body instanceof JSONObject;
        boolean isJsonArray = body instanceof JSONArray;
        if(!isJsonObject && !isJsonArray){
            return model;
        }
        Type typeArgument = TypeUtil.getTypeArgument(getClass(),0);
        body = JSONUtil.toBean(JSONUtil.toJsonStr(body),typeArgument,true);
        model.setBody(body);
        return model;
    }
}
