package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class IntegerColumnConvert implements ColumnConvert<Integer> {

    @Override
    public Integer convert(Object value) {
        return Convert.toInt(value);
    }
}
