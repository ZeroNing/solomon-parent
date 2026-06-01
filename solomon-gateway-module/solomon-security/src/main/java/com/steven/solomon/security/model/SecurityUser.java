package com.steven.solomon.security.model;

import cn.hutool.core.util.StrUtil;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 当前登录用户。
 */
public record SecurityUser(
    String username,
    String tenantCode,
    String tenantId,
    String tenantName,
    List<GrantedAuthority> authorities) implements UserDetails, Serializable {

  public static SecurityUser of(
      String username,
      String tenantCode,
      String tenantId,
      String tenantName,
      Collection<String> authorities) {
    List<GrantedAuthority> grantedAuthorities = authorities == null
        ? List.of()
        : authorities.stream()
            .filter(StrUtil::isNotBlank)
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .toList();
    return new SecurityUser(username, tenantCode, tenantId, tenantName, grantedAuthorities);
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
