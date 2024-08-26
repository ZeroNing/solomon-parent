package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class LongColumnConvert implements ColumnConvert<Long> {

    @Override
    public Long convert(Object value) {
        return Convert.toLong(value);
    }
}
