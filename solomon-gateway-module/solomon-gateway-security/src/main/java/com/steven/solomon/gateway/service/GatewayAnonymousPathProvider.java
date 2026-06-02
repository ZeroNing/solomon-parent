package com.steven.solomon.gateway.service;

import java.util.Collection;

/** 提供允许匿名访问的接口路径。 */
public interface GatewayAnonymousPathProvider {

  Collection<String> paths();
}
