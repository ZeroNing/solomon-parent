package com.steven.solomon.service.impl;

import com.steven.solomon.service.AbsICacheService;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultService extends AbsICacheService {


  @Override
  public void expire(String group, String key, int time) {
  }

  @Override
  public long getExpire(String group, String key) {
    return 0;
  }

  @Override
  public boolean hasKey(String group, String key) {
    return false;
  }

  @Override
  public void del(String group, String... key) {

  }

  @Override
  public <T> T get(String group, String key) {
    return null;
  }

  @Override
  public <T> T set(String group, String key, T value) {
    return null;
  }

  @Override
  public <T> T set(String group, String key, T value, int time) {
    return null;
  }

  @Override
  public <T extends Set> T setGet(String group, String key) {
    return null;
  }

  @Override
  public <T extends Set> T set(String group, String key, int time, T... value) {
    return null;
  }

  @Override
  public <T extends Set> T set(String group, String key, T... value) {
    return null;
  }

  @Override
  public Long remove(String group, String key, Object... value) {
    return null;
  }

  @Override
  public <T> T ListGet(String group, String key, int start, int end) {
    return null;
  }

  @Override
  public <T> T leftPush(String group, String key, T value, int time) {
    return null;
  }

  @Override
  public <T> T leftPush(String group, String key, T value) {
    return null;
  }

  @Override
  public <T> T leftPushAll(String group, String key, int time, T... value) {
    return null;
  }

  @Override
  public <T> T leftPushAll(String group, String key, int time, List<T> value) {
    return null;
  }

  @Override
  public <T> T leftPushAll(String group, String key, T... value) {
    return null;
  }

  @Override
  public <T> T leftPushAll(String group, String key, List<T> value) {
    return null;
  }

  @Override
  public <T> T rightPush(String group, String key, T value, int time) {
    return null;
  }

  @Override
  public <T> T rightPushAll(String group, String key, int time, T... value) {
    return null;
  }

  @Override
  public <T> T rightPushAll(String group, String key, int time, List<T> value) {
    return null;
  }

  @Override
  public <T> T rightPushAll(String group, String key, T... value) {
    return null;
  }

  @Override
  public <T> T rightPushAll(String group, String key, List<T> value) {
    return null;
  }

  @Override
  public <T> T rightPush(String group, String key, T value) {
    return null;
  }

  @Override
  public <T> T hashGet(String group, String key, String hashKey) {
    return null;
  }

  @Override
  public <T> T put(String group, String key, String hashKey, T value) {
    return null;
  }

  @Override
  public <T> T put(String group, String key, String hashKey, T value, int time) {
    return null;
  }

  @Override
  public <T extends Map> T putAll(String group, String key, T value, int time) {
    return null;
  }

  @Override
  public <T extends Map> T putAll(String group, String key, T value) {
    return null;
  }

  @Override
  public Long delete(String group, String key, Object... hashKey) {
    return null;
  }

  @Override
  public boolean lockSet(String group, String key, Object value, int time) {
    return false;
  }

  @Override
  public void deleteLock(String group, String key) {

  }
}
