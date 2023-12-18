package com.steven.solomon.utils.excel;

import cn.hutool.core.date.StopWatch;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.steven.solomon.clazz.ClassUtils;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel工具类
 */
public class ExcelUtils {

	private static final Logger logger = LoggerUtils.logger(ExcelUtils.class);

	/**
	 * 导出
	 * @param response
	 * @param excelName 文件名需要带上后缀名
	 * @param sheetName 表名 不填默认为sheet
	 * @param clazz     需要导出excel的类,其中ExcelProperty注解国际化是类名+.+字段名组成
	 * @param data      数据
	 * @throws Exception
	 */
	public static void export(HttpServletResponse response, String excelName, String sheetName,Class<?> clazz,List<?> data) throws Exception {
		setHead(response, excelName);
		//更新Class注解值
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		logger.debug("开始更新Class注解值国际化");
		updateClassExcelPropertyValue(clazz);
		stopWatch.stop();
		logger.debug("结束更新Class注解值国际化,耗时:{}秒",stopWatch.getTotalTimeSeconds());
		//开始导出
		stopWatch = new StopWatch();
		stopWatch.start();
		logger.debug("开始导出Excel");
		EasyExcel.write(response.getOutputStream(), clazz)
				.registerWriteHandler(formatExcel())
				.registerWriteHandler(new ExcelWidthStyleStrategy())
				.sheet(0,ValidateUtils.getOrDefault(sheetName,"sheet")).doWrite(data);
		stopWatch.stop();
		logger.debug("结束导出Excel,耗时:{}秒",stopWatch.getTotalTimeSeconds());
	}

	/**
	 * 导出
	 * @param excelName 文件名需要带上后缀名
	 * @param sheetName 表名 不填默认为sheet
	 * @param clazz     需要导出excel的类,其中ExcelProperty注解国际化是类名+.+字段名组成
	 * @param data      数据
	 * @return          文件
	 * @throws Exception
	 */
	public static MultipartFile export(String excelName, String sheetName, Class<?> clazz,List<?> data) throws Exception {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
			//更新Class注解值
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.debug("开始更新Class注解值国际化");
			updateClassExcelPropertyValue(clazz);
			stopWatch.stop();
			logger.debug("结束更新Class注解值国际化,耗时:{}秒",stopWatch.getTotalTimeSeconds());
			//开始导出
			stopWatch = new StopWatch();
			stopWatch.start();
			logger.debug("开始导出Excel");
			ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(os, clazz).registerWriteHandler(formatExcel()).registerWriteHandler(new ExcelWidthStyleStrategy());
			ExcelWriter excelWriter = excelWriterBuilder.build();
			ExcelWriterSheetBuilder excelWriterSheetBuilder;
			WriteSheet              writeSheet;
			excelWriterSheetBuilder = new ExcelWriterSheetBuilder(excelWriter);
			excelWriterSheetBuilder.sheetNo(0).sheetName(sheetName);
			writeSheet = excelWriterSheetBuilder.build();
			excelWriter.write(data, writeSheet);
			// 必须要finish才会写入，不finish只会创建empty的文件
			excelWriter.finish();
			stopWatch.stop();
			logger.debug("结束导出Excel,耗时:{}秒",stopWatch.getTotalTimeSeconds());
			byte[] content = os.toByteArray();
			//生成文件
			try(InputStream is = new ByteArrayInputStream(content);){
				return new MockMultipartFile(excelName,excelName, MediaType.MULTIPART_FORM_DATA_VALUE, is);
			}
		}
	}

	private static void updateClassExcelPropertyValue(Class<?> clazz) throws Exception {
		for(Field field : clazz.getDeclaredFields()){
			String i18nKey = clazz.getSimpleName()+"."+field.getName();
			Map<String,Object> annotationNameAndValueMap = new HashMap<>();
			annotationNameAndValueMap.put("value", I18nUtils.getMessage(i18nKey,null));
			ClassUtils.updateClassField(field,ExcelProperty.class,annotationNameAndValueMap);
		}
	}

	/**
	 * 设置响应头
	 *
	 * @param response 回应的请求数据
	 * @param fileName 文件名字
	 */
	private static void setHead(HttpServletResponse response, String fileName) {

		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		// 这里URLEncoder.encode可以防止中文乱码
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		response.setHeader("Content-disposition", "attachment;filename=" + fileName);
	}

	public static HorizontalCellStyleStrategy formatExcel() {
		WriteCellStyle headWriteCellStyle = new WriteCellStyle();
		headWriteCellStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
		WriteFont headWriteFont = new WriteFont();
		headWriteFont.setFontHeightInPoints((short) 10);
		headWriteCellStyle.setWriteFont(headWriteFont);
		// 内容的策略
		WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
		WriteFont      contentWriteFont      = new WriteFont();
		// 字体大小
		contentWriteFont.setFontHeightInPoints((short) 10);
		contentWriteCellStyle.setWriteFont(contentWriteFont);
		// 设置自动换行
		contentWriteCellStyle.setWrapped(false);
		// 设置垂直居中
		contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// 设置水平居中
		contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

		return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

	}

	/**
	 * 设置头部单元格宽度
	 */
	public static class ExcelWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

		@Override
		protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> list, Cell cell, Head head,
				Integer integer, Boolean aBoolean) {
			// 设置宽度
			Sheet sheet = writeSheetHolder.getSheet();
			sheet.setColumnWidth(cell.getColumnIndex(), 5000);
		}
	}

}
