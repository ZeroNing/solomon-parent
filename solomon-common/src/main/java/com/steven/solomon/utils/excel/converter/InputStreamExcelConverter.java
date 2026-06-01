package com.steven.solomon.utils.excel.converter;

import cn.hutool.core.util.ObjectUtil;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.idev.excel.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamExcelConverter implements Converter<InputStream> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return InputStream.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(InputStream value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws IOException {
        try {
            if (ObjectUtil.isEmpty(value)) {
                return new WriteCellData<>("InputStream为空");
            }
            byte[] bytes = IoUtils.toByteArray(value);
            return new WriteCellData<>(bytes);
        }catch (Exception e) {
            return new WriteCellData<>("InputStream异常");
        } finally {
            if (ObjectUtil.isNotEmpty(value)) {
                value.close();
            }
        }
    }
}
