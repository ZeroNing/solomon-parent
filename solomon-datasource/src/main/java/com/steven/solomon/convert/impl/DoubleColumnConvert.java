package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class DoubleColumnConvert implements ColumnConvert<Double> {

    @Override
    public Double convert(Object value) {
        return Convert.toDouble(value);
    }
}
