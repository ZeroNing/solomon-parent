package com.steven.solomon.convert.impl;

import cn.hutool.core.convert.Convert;
import com.steven.solomon.convert.ColumnConvert;

import java.time.LocalDateTime;
import java.util.Date;

public class LocalDateTimeColumnConvert implements ColumnConvert<LocalDateTime> {

    @Override
    public LocalDateTime convert(Object value) {
        return Convert.toLocalDateTime(value);
    }
}
