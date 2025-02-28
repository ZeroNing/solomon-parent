package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

import java.math.BigInteger;

public class BigIntegerColumnConvert implements ColumnConvert<BigInteger> {

    @Override
    public BigInteger convert(Object value) {
        return Convert.toBigInteger(value);
    }
}
