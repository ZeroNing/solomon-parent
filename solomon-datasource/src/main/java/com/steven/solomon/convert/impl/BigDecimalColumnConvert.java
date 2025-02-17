package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

import java.math.BigDecimal;

public class BigDecimalColumnConvert implements ColumnConvert<BigDecimal> {

    @Override
    public BigDecimal convert(Object value) {
        return Convert.toBigDecimal(value);
    }
}
