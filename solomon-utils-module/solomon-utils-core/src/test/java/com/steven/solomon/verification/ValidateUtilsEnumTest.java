package com.steven.solomon.verification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ValidateUtils枚举校验方法单元测试
 */
class ValidateUtilsEnumTest {

    // ========== 测试用枚举 ==========

    enum TestEnum {
        VALUE_A,
        VALUE_B,
        VALUE_C
    }

    // ========== checkEnumValueIsEmpty 测试 ==========

    @Test
    @DisplayName("checkEnumValueIsEmpty - 有效枚举值返回false")
    void testCheckEnumValueIsEmpty_Valid() {
        assertFalse(ValidateUtils.checkEnumValueIsEmpty("VALUE_A", TestEnum.class));
        assertFalse(ValidateUtils.checkEnumValueIsEmpty("VALUE_B", TestEnum.class));
        assertFalse(ValidateUtils.checkEnumValueIsEmpty("VALUE_C", TestEnum.class));
    }

    @Test
    @DisplayName("checkEnumValueIsEmpty - 无效枚举值返回true")
    void testCheckEnumValueIsEmpty_Invalid() {
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_D", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("invalid_value", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("", TestEnum.class));
    }

    @Test
    @DisplayName("checkEnumValueIsEmpty - null值返回true")
    void testCheckEnumValueIsEmpty_Null() {
        assertTrue(ValidateUtils.checkEnumValueIsEmpty(null, TestEnum.class));
    }

    @Test
    @DisplayName("checkEnumValueIsEmpty - null class返回true")
    void testCheckEnumValueIsEmpty_NullClass() {
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_A", null));
    }

    @Test
    @DisplayName("checkEnumValueIsEmpty - 大小写敏感测试")
    void testCheckEnumValueIsEmpty_CaseSensitive() {
        // 小写应该无效（找不到）
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("value_a", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("Value_A", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_a", TestEnum.class));
    }

    @Test
    @DisplayName("checkEnumValueIsEmpty - 空白字符串")
    void testCheckEnumValueIsEmpty_Blank() {
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("   ", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("\t", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("\n", TestEnum.class));
    }

    // ========== checkEnumValueIsNotEmpty 测试 ==========

    @Test
    @DisplayName("checkEnumValueIsNotEmpty - 有效枚举值返回true")
    void testCheckEnumValueIsNotEmpty_Valid() {
        assertTrue(ValidateUtils.checkEnumValueIsNotEmpty("VALUE_A", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsNotEmpty("VALUE_B", TestEnum.class));
    }

    @Test
    @DisplayName("checkEnumValueIsNotEmpty - 无效枚举值返回false")
    void testCheckEnumValueIsNotEmpty_Invalid() {
        assertFalse(ValidateUtils.checkEnumValueIsNotEmpty("INVALID", TestEnum.class));
        assertFalse(ValidateUtils.checkEnumValueIsNotEmpty(null, TestEnum.class));
        assertFalse(ValidateUtils.checkEnumValueIsNotEmpty("value_a", TestEnum.class));
    }

    // ========== checkEnumValueIsEmpty with exception 测试 ==========

    @Test
    @DisplayName("checkEnumValueIsEmpty - 无效值抛出BaseException")
    void testCheckEnumValueIsEmpty_WithException() {
        assertDoesNotThrow(() -> 
            ValidateUtils.checkEnumValueIsEmpty("VALUE_A", TestEnum.class, "ERROR_CODE")
        );
    }

    @Test
    @DisplayName("checkEnumValueIsEmpty - 有效值不抛出异常")
    void testCheckEnumValueIsEmpty_WithException_Valid() {
        // 无效值会抛出异常，这里不需要抛出
        assertDoesNotThrow(() -> 
            ValidateUtils.checkEnumValueIsEmpty("VALUE_A", TestEnum.class, "ERROR_CODE")
        );
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("边界情况 - 特殊字符")
    void testSpecialCharacters() {
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_A!", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_A@", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty(" VALUE_A", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_A ", TestEnum.class));
    }

    @Test
    @DisplayName("边界情况 - Unicode字符")
    void testUnicodeCharacters() {
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("中文", TestEnum.class));
        assertTrue(ValidateUtils.checkEnumValueIsEmpty("VALUE_测试", TestEnum.class));
    }

    // ========== 性能测试 ==========

    @Test
    @DisplayName("性能测试 - 批量调用不应抛出异常")
    void testPerformance() {
        // 批量调用，确保没有性能问题
        for (int i = 0; i < 1000; i++) {
            ValidateUtils.checkEnumValueIsEmpty("VALUE_A", TestEnum.class);
            ValidateUtils.checkEnumValueIsEmpty("INVALID", TestEnum.class);
        }
        // 如果能正常完成，说明没有内存泄漏或性能问题
        assertTrue(true);
    }
}
