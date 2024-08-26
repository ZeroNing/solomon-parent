package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class StringColumnConvert implements ColumnConvert<String> {

    @Override
    public String convert(Object value) {
        return Convert.toStr(value);
    }
}
