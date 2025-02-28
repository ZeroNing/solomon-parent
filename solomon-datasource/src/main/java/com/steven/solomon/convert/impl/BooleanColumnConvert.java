package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class BooleanColumnConvert implements ColumnConvert<Boolean> {

    @Override
    public Boolean convert(Object value) {
        return Convert.toBool(value.toString());
    }

}
