package com.steven.solomon.converter;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Date 转 LocalDate 转换器。
 *
 * <p>用于MongoDB读写时的类型自动转换，将 {@link Date} 转为 {@link LocalDate}。</p>
 */
public class DateToLocalDateConverter  implements Converter<Date, LocalDate> {

    /**
     * 将Date转换为LocalDate，使用系统默认时区。
     *
     * @param date Date对象
     * @return 对应的LocalDate
     */
    @Override
    public LocalDate convert(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
