package com.steven.solomon.json;

import cn.hutool.core.lang.TypeReference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.steven.solomon.verification.ValidateUtils;
import java.io.IOException;

public class FastJsonUtils {

  /**
   * 转换json
   * @param result
   * @return
   * @throws JsonProcessingException
   */
  public static String formatJsonByFilter(Object result, SerializeFilter... filter){
    return JSONObject.toJSONString(result,filter);
  }

  /**
   * 转换json
   * @param result
   * @return
   * @throws JsonProcessingException
   */
  public static byte[] formatBytesByFilter(Object result, SerializeFilter... filter){
    return JSONObject.toJSONBytes(result,filter);
  }

  /**
   * 转换对象
   */
  public static <T> T conversionClass(String s, Class<T> t) throws IOException {
    return JSONObject.parseObject(s,t);
  }

  /**
   * 转换数组对象
   */
  public static <T> T conversionClassList(String s,String arrayString, TypeReference<T> jsonTypeReference) throws IOException {
    String json = s;
    if(ValidateUtils.isNotEmpty(arrayString)){
      JSONObject jsonObject = JSON.parseObject(s);
      JSONArray jsonArray = jsonObject.getJSONArray(arrayString);
      json = jsonArray.toJSONString();
    }
    return JSONObject.parseObject(json, jsonTypeReference);
  }

  /**
   * 获取jsonObject对象
   */
  public static JSONObject conversionJsonArray(String s){
    return JSON.parseObject(s);
  }

  /**
   * 获取json字符串节点数据
   * @param s json
   * @param arrayString 节点数据key
   */
  public static JSONArray conversionJsonArray(String s,String arrayString){
    JSONObject jsonObject = conversionJsonArray(s);
    return jsonObject.getJSONArray(arrayString);
  }

  /**
   * 获取json字符串节点数据
   * @param jsonObject 获取jsonObject对象
   * @param arrayString 节点数据key
   */
  public static JSONArray conversionJsonArray(JSONObject jsonObject,String arrayString){
    return jsonObject.getJSONArray(arrayString);
  }
}
