package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

public class BytesColumnConvert implements ColumnConvert<Byte[]> {

    @Override
    public Byte[] convert(Object value) {
        return Convert.toByteArray(value);
    }
}
