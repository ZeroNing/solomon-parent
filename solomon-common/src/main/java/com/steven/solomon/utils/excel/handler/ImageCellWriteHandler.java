package com.steven.solomon.utils.excel.handler;

import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.ImageData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import com.steven.solomon.verification.ValidateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Excel 图片单元格写入处理器。
 *
 * <p>FastExcel 默认图片写入不便于控制一格多图的位置，本处理器在单元格写入后重新插入图片，
 * 并根据同一单元格图片数量调整列宽。</p>
 */
public class ImageCellWriteHandler implements CellWriteHandler {

    private final HashMap<String, List<ImageData>> imageDataMap = new HashMap<>(16);

    /**
     * 单元格的图片最大张数（每列的单元格图片张数不确定，单元格宽度需按照张数最多的长度来设置）
     */
    private final AtomicReference<Integer> maxImageSize = new AtomicReference<>(0);

    /**
     * 默认图片宽度（单位像素）：60
     */
    private static final int DEFAULT_IMAGE_WIDTH = 60;

    /**
     * 默认像素转换因子：32
     */
    private static final int DEFAULT_PIXEL_CONVERSION_FACTOR = 32;

    /**
     * 图片宽度，单位像素
     */
    private final int imageWidth;

    /**
     * 像素转换因子
     */
    private final int pixelConversionFactor;

    public ImageCellWriteHandler() {
        this.imageWidth = DEFAULT_IMAGE_WIDTH;
        this.pixelConversionFactor = DEFAULT_PIXEL_CONVERSION_FACTOR;
    }

    public ImageCellWriteHandler(int imageWidth, int pixelConversionFactor) {
        this.imageWidth = imageWidth;
        this.pixelConversionFactor = pixelConversionFactor;
    }

    @Override
    public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, WriteCellData<?> cellData, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        //  在数据转换成功后 不是头就把类型设置成空
        if (isHead) {
            return;
        }
        //将要插入图片的单元格的type设置为空,下面再填充图片
        if (ValidateUtils.isNotEmpty(cellData.getImageDataList())) {
            List<ImageData> imageDataList = cellData.getImageDataList();
            maxImageSize.updateAndGet(size -> Math.max(size, imageDataList.size()));
            imageDataMap.put(getCellKey(cell), imageDataList);
            cellData.setType(CellDataTypeEnum.EMPTY);
            cellData.setImageDataList(new ArrayList<>());
        }
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        //  在单元格写入完毕后 ，自己填充图片
        if (isHead || ValidateUtils.isEmpty(cellDataList)) {
            return;
        }
        Sheet sheet = cell.getSheet();
        WriteCellData<?> writeCellData = cellDataList.getFirst();
        CellDataTypeEnum type = writeCellData.getType();

        if (type != CellDataTypeEnum.EMPTY) {
            return;
        }
        List<ImageData> imageDataList = imageDataMap.get(getCellKey(cell));
        if (ValidateUtils.isEmpty(imageDataList)) {
            return;
        }

        int widthValue =  imageWidth * pixelConversionFactor;
        sheet.setColumnWidth(cell.getColumnIndex(), widthValue * maxImageSize.get() + pixelConversionFactor);
        insertImages(sheet, cell, imageDataList);
    }


    /**
     * 重新插入一个图片
     *
     * @param sheet       Excel页面
     * @param cell        表格元素
     * @param pictureData 图片数据
     * @param i           图片顺序
     */
    public int insertImage(Sheet sheet, Cell cell, byte[] pictureData, int i) {
        int picWidth = Units.pixelToEMU(imageWidth);
        int index = sheet.getWorkbook().addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
        Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            drawing = sheet.createDrawingPatriarch();
        }
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        /*
         * 设置图片坐标
         * 为了不让图片遮挡单元格的上边框和右边框，故 x1、x2、y1 这几个坐标点均向后移动了一个像素点
         */
        anchor.setDx1(Units.pixelToEMU(1) + picWidth * i);
        anchor.setDx2(Units.pixelToEMU(1) + picWidth + picWidth * i);
        anchor.setDy1(Units.pixelToEMU(1));
        anchor.setDy2(0);
        //设置图片位置
        int columnIndex = cell.getColumnIndex();
        anchor.setCol1(columnIndex);
        anchor.setCol2(columnIndex);
        int rowIndex = cell.getRowIndex();
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 1);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        drawing.createPicture(anchor, index);
        return index;
    }

    private void insertImages(Sheet sheet, Cell cell, List<ImageData> imageDataList) {
        for (int i = 0; i < imageDataList.size(); i++) {
            insertImage(sheet, cell, imageDataList.get(i).getImage(), i);
        }
    }

    private String getCellKey(Cell cell) {
        return cell.getRowIndex() + "_" + cell.getColumnIndex();
    }
}
