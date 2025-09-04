package com.steven.solomon.utils.excel.converter;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ImageData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.idev.excel.util.IoUtils;
import com.steven.solomon.verification.ValidateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListExcelConverter implements Converter<List<?>> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(List<?> list, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws IOException {
        if (ValidateUtils.isEmpty(list)){
            return new WriteCellData<>("");
        }
        Object value = list.getFirst();
        boolean isInputStream = value instanceof InputStream;
        try {
            if(isInputStream){
                List<ImageData> imageDataList = new ArrayList<>();
                WriteCellData<?> writeCellData = new WriteCellData<>();
                for(Object val : list){
                    InputStream inputStream = (InputStream) val;
                    ImageData imageData = new ImageData();
                    imageData.setImage(IoUtils.toByteArray(inputStream));
                    imageDataList.add(imageData);
                }
                writeCellData.setType(CellDataTypeEnum.EMPTY);
                writeCellData.setImageDataList(imageDataList);
                return writeCellData;
            } else {
                List<String> stringList = new ArrayList<>();
                for(Object val : list){
                    stringList.add(val.toString());
                }
                return new WriteCellData<>(stringList.toString());
            }
        }catch (Exception e){
            return new WriteCellData<>("InputStream异常");
        } finally {
            if (ValidateUtils.isNotEmpty(list) && isInputStream){
                for(Object val : list){
                    InputStream inputStream = (InputStream) val;
                    inputStream.close();
                }
            }
        }
    }
}
