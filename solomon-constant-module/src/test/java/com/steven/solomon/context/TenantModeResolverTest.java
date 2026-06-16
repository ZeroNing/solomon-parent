package com.steven.solomon.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TenantModeResolverTest {

  @Test
  void useDefaultTenantInSingleMode() {
    assertEquals("default", new TenantModeResolver(new TenantModeProperties()).resolve(null));
  }

  @Test
  void trimSpecifiedTenant() {
    assertEquals("tenant-1",
        new TenantModeResolver(new TenantModeProperties()).resolve(" tenant-1 "));
  }

  @Test
  void requireTenantInMultiMode() {
    TenantModeProperties properties = new TenantModeProperties();
    properties.setMode(TenantMode.MULTI);

    assertThrows(IllegalArgumentException.class,
        () -> new TenantModeResolver(properties).resolve(null));
  }

  @Test
  void allowMissingTenantWhenMultiModeIsNotStrict() {
    TenantModeProperties properties = new TenantModeProperties();
    properties.setMode(TenantMode.MULTI);
    properties.setRequireCodeInMultiMode(false);

    assertNull(new TenantModeResolver(properties).resolve(null));
  }

  @Test
  void rejectMissingSingleTenantDefault() {
    TenantModeProperties properties = new TenantModeProperties();
    properties.setDefaultCode(" ");

    assertThrows(IllegalArgumentException.class, () -> new TenantModeResolver(properties));
  }
}
