package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class ShortColumnConvert implements ColumnConvert<Short> {

    @Override
    public Short convert(Object value) {
        return Convert.toShort(value);
    }
}
