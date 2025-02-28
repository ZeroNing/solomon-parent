package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

import java.util.Date;

public class DateColumnConvert implements ColumnConvert<Date> {

    @Override
    public Date convert(Object value) {
        return Convert.toDate(value);
    }
}
