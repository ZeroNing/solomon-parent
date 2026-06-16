package com.steven.solomon.utils.excel;


import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

public class ExcelWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> list, Cell cell, Head head,
                                  Integer integer, Boolean aBoolean) {
        // 设置宽度
        Sheet sheet = writeSheetHolder.getSheet();
        sheet.setColumnWidth(cell.getColumnIndex(), 5000);
    }
}
