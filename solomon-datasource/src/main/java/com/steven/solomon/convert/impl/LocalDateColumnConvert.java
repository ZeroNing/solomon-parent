package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

import java.time.LocalDate;

public class LocalDateColumnConvert implements ColumnConvert<LocalDate> {

    @Override
    public LocalDate convert(Object value) {
        return Convert.toLocalDateTime(value).toLocalDate();
    }
}
