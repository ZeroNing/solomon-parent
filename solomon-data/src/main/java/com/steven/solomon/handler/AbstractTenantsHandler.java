package com.steven.solomon.handler;

import java.util.List;
import java.util.Map;

public interface AbstractTenantsHandler<F,P> {

  F getFactory();

  void removeFactory();

  void setFactory(String name);

  void setProperties(P properties);

  List<P> getProperties();

  Map<String,F> getFactoryMap();

  void setFactoryMap(Map<String, F> redisFactoryMap);

  void setFactory(String name,F factory);
}
