package com.steven.solomon.security.core.permission;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;

class InMemoryPermissionStoreTest {

    @Test
    void shouldInvalidateByCodeAndAdvanceVersion() {
        InMemoryPermissionStore store = new InMemoryPermissionStore();
        store.saveAll(List.of(
                new PermissionInfo("USER:READ", "read user", "/users/{id}", "GET", false),
                new PermissionInfo("USER:CREATE", "create user", "/users", "POST", false)));
        long savedVersion = store.version();

        assertThat(store.invalidateByCode("USER:READ")).isTrue();

        assertThat(store.findAll()).extracting(PermissionInfo::code).containsExactly("USER:CREATE");
        assertThat(store.version()).isGreaterThan(savedVersion);
    }

    @Test
    void shouldInvalidateByPathAndMethod() {
        InMemoryPermissionStore store = new InMemoryPermissionStore();
        store.saveAll(List.of(
                new PermissionInfo("USER:READ", "read user", "/users", "GET", true),
                new PermissionInfo("USER:CREATE", "create user", "/users", "POST", false)));

        assertThat(store.invalidateByPath("/users", "get")).isTrue();

        assertThat(store.findAll()).extracting(PermissionInfo::code).containsExactly("USER:CREATE");
        assertThat(store.findAnonymousPaths()).isEmpty();
    }

    @Test
    void shouldInvalidateAllAndAdvanceVersion() {
        InMemoryPermissionStore store = new InMemoryPermissionStore();
        store.saveAll(List.of(new PermissionInfo("USER:READ", "read user", "/users", "GET", false)));
        long savedVersion = store.version();

        store.invalidateAll();

        assertThat(store.findAll()).isEmpty();
        assertThat(store.version()).isGreaterThan(savedVersion);
    }
}
