package com.steven.solomon.service.impl;

import com.steven.solomon.service.AbsICacheService;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class DefaultService extends AbsICacheService {


  @Override
  public boolean expire(String group, String key, int time) {
    return false;
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
  public boolean lockSet(String group, String key, Object value, int time) {
    return false;
  }

  @Override
  public void deleteLock(String group, String key) {

  }
}
