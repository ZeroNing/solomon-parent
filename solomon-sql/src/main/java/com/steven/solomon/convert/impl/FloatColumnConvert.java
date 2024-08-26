package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class FloatColumnConvert implements ColumnConvert<Float> {

    @Override
    public Float convert(Object value) {
        return Convert.toFloat(value);
    }
}
