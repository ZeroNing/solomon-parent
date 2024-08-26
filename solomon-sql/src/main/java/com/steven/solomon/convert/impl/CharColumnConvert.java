package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class CharColumnConvert implements ColumnConvert<Character> {

    @Override
    public Character convert(Object value) {
        return Convert.toChar(value);
    }
}
