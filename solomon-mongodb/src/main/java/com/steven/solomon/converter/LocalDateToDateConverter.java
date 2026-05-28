package com.steven.solomon.converter;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalDate 转 Date 转换器。
 *
 * <p>用于MongoDB读写时的类型自动转换，将 {@link LocalDate} 转为 {@link Date}。</p>
 */
public class LocalDateToDateConverter implements Converter<LocalDate, Date> {

    /**
     * 将LocalDate转换为Date，取当天起始时间（00:00），使用系统默认时区。
     *
     * @param source LocalDate对象
     * @return 对应当天起始时间的Date
     */
    @Override
    public Date convert(LocalDate source) {
        return Date.from(source.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
