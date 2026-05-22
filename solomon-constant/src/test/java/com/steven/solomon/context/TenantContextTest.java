package com.steven.solomon.context;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TenantContext单元测试
 */
class TenantContextTest {

    private TestTenantContext context;

    @BeforeEach
    void setUp() {
        context = new TestTenantContext();
    }

    @AfterEach
    void tearDown() {
        context.removeFactory();
    }

    // ========== registerFactory 测试 ==========

    @Test
    @DisplayName("registerFactory - 正常注册工厂")
    void testRegisterFactory_Success() {
        String factory = "test-factory";
        context.registerFactory("tenant-001", factory);

        assertTrue(context.isRegistered("tenant-001"));
        assertEquals(1, context.getFactoryMap().size());
    }

    @Test
    @DisplayName("registerFactory - null tenantId抛出NullPointerException")
    void testRegisterFactory_NullTenantId() {
        assertThrows(NullPointerException.class, () -> 
            context.registerFactory(null, "factory")
        );
    }

    @Test
    @DisplayName("registerFactory - null factory抛出NullPointerException")
    void testRegisterFactory_NullFactory() {
        assertThrows(NullPointerException.class, () -> 
            context.registerFactory("tenant-001", null)
        );
    }

    @Test
    @DisplayName("registerFactory - 覆盖已存在的工厂")
    void testRegisterFactory_Override() {
        context.registerFactory("tenant-001", "factory-1");
        context.registerFactory("tenant-001", "factory-2");

        assertEquals("factory-2", context.getFactoryMap().get("tenant-001"));
    }

    // ========== setFactory 测试 ==========

    @Test
    @DisplayName("setFactory - 正常设置工厂")
    void testSetFactory_Success() {
        context.registerFactory("tenant-001", "test-factory");
        context.setFactory("tenant-001");

        assertEquals("test-factory", context.getFactory());
    }

    @Test
    @DisplayName("setFactory - 未注册的租户抛出IllegalStateException")
    void testSetFactory_NotRegistered() {
        assertThrows(IllegalStateException.class, () -> 
            context.setFactory("unknown-tenant")
        );
    }

    // ========== trySetFactory 测试 ==========

    @Test
    @DisplayName("trySetFactory - 任务执行后自动清理ThreadLocal")
    void testTrySetFactory_AutoCleanup() {
        context.registerFactory("tenant-001", "factory-1");

        context.trySetFactory("tenant-001", () -> {
            // 在任务中，ThreadLocal有值
            assertEquals("factory-1", context.getFactory());
        });

        // 任务结束后，ThreadLocal应该被清理
        assertNull(context.getFactory());
    }

    @Test
    @DisplayName("trySetFactory - 任务抛异常时也能清理ThreadLocal")
    void testTrySetFactory_ExceptionCleanup() {
        context.registerFactory("tenant-001", "factory-1");

        assertThrows(RuntimeException.class, () -> 
            context.trySetFactory("tenant-001", () -> {
                throw new RuntimeException("test exception");
            })
        );

        // 即使异常，ThreadLocal也应该被清理
        assertNull(context.getFactory());
    }

    @Test
    @DisplayName("trySetFactory - null任务抛出NullPointerException")
    void testTrySetFactory_NullTask() {
        context.registerFactory("tenant-001", "factory-1");

        assertThrows(NullPointerException.class, () -> 
            context.trySetFactory("tenant-001", null)
        );
    }

    // ========== unregisterFactory 测试 ==========

    @Test
    @DisplayName("unregisterFactory - 正常注销工厂")
    void testUnregisterFactory_Success() {
        context.registerFactory("tenant-001", "factory-1");
        assertTrue(context.isRegistered("tenant-001"));

        String removed = context.unregisterFactory("tenant-001");

        assertEquals("factory-1", removed);
        assertFalse(context.isRegistered("tenant-001"));
    }

    @Test
    @DisplayName("unregisterFactory - 注销不存在的工厂返回null")
    void testUnregisterFactory_NotExists() {
        String removed = context.unregisterFactory("unknown");
        assertNull(removed);
    }

    // ========== isRegistered 测试 ==========

    @Test
    @DisplayName("isRegistered - 检查注册状态")
    void testIsRegistered() {
        assertFalse(context.isRegistered("tenant-001"));

        context.registerFactory("tenant-001", "factory-1");
        assertTrue(context.isRegistered("tenant-001"));

        context.unregisterFactory("tenant-001");
        assertFalse(context.isRegistered("tenant-001"));
    }

    // ========== registerFactories 测试 ==========

    @Test
    @DisplayName("registerFactories - 批量注册工厂")
    void testRegisterFactories_Success() {
        Map<String, String> factories = new HashMap<>();
        factories.put("tenant-001", "factory-1");
        factories.put("tenant-002", "factory-2");

        context.registerFactories(factories);

        assertEquals(2, context.getFactoryMap().size());
        assertTrue(context.isRegistered("tenant-001"));
        assertTrue(context.isRegistered("tenant-002"));
    }

    @Test
    @DisplayName("registerFactories - null参数抛出NullPointerException")
    void testRegisterFactories_Null() {
        assertThrows(NullPointerException.class, () -> 
            context.registerFactories(null)
        );
    }

    // ========== getFactoryMap 不可修改测试 ==========

    @Test
    @DisplayName("getFactoryMap - 返回不可修改的Map")
    void testGetFactoryMap_Unmodifiable() {
        context.registerFactory("tenant-001", "factory-1");
        Map<String, String> map = context.getFactoryMap();

        assertThrows(UnsupportedOperationException.class, () -> 
            map.put("tenant-002", "factory-2")
        );
    }

    // ========== 多线程测试 ==========

    @Test
    @DisplayName("trySetFactory - 多线程隔离测试")
    void testTrySetFactory_ThreadIsolation() throws InterruptedException {
        context.registerFactory("tenant-001", "factory-1");
        context.registerFactory("tenant-002", "factory-2");

        AtomicBoolean thread1Success = new AtomicBoolean(false);
        AtomicBoolean thread2Success = new AtomicBoolean(false);

        Thread t1 = new Thread(() -> {
            context.trySetFactory("tenant-001", () -> {
                assertEquals("factory-1", context.getFactory());
                thread1Success.set(true);
            });
        });

        Thread t2 = new Thread(() -> {
            context.trySetFactory("tenant-002", () -> {
                assertEquals("factory-2", context.getFactory());
                thread2Success.set(true);
            });
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(thread1Success.get());
        assertTrue(thread2Success.get());
    }

    // ========== 测试用实现类 ==========

    private static class TestTenantContext extends TenantContext<String> {
        // 使用String作为工厂类型进行测试
    }
}
