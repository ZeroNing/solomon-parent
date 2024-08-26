package com.steven.solomon.enums;

import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.convert.impl.*;

import java.math.BigDecimal;

public enum ConvertEnums {

    STRING_CONVERT(String.class, new StringColumnConvert()),
    SHORT_CONVERT(Short.class, new ShortColumnConvert()),
    LONG_CONVERT(Long.class, new LongColumnConvert()),
    INTEGER_CONVERT(Integer.class, new IntegerColumnConvert()),
    FLOAT_CONVERT(Float.class, new FloatColumnConvert()),
    DOUBLE_CONVERT(Double.class, new DoubleColumnConvert()),
    CHAR_CONVERT(Character.class, new CharColumnConvert()),
    BYTE_CONVERT(Byte.class, new ByteColumnConvert()),
    BOOLEAN_CONVERT(Boolean.class, new BooleanColumnConvert()),
    BIGDECIMAL_CONVERT(BigDecimal.class, new BigDecimalColumnConvert()),
    ;

    private Class clazz;

    private ColumnConvert convert;

    ConvertEnums(Class clazz, ColumnConvert convert) {
        this.clazz = clazz;
        this.convert = convert;
    }

    public Class getClazz() {
        return clazz;
    }

    public ColumnConvert getConvert() {
        return convert;
    }
}
