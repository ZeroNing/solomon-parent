package com.steven.solomon.utils.excel;

import cn.hutool.core.date.StopWatch;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.write.builder.ExcelWriterBuilder;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import cn.idev.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.steven.solomon.clazz.ClassUtils;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.file.MockMultipartFile;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
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
	 * @param excelName 文件名需要带上后缀名
	 * @param sheetName 表名 不填默认为sheet
	 * @param clazz     需要导出excel的类,其中ExcelProperty注解国际化是类名+.+字段名组成
	 * @param data      数据
	 */
	public static void export(HttpServletResponse response, String excelName, String sheetName, Class<?> clazz, List<?> data, HorizontalCellStyleStrategy cellStyleStrategy, AbstractColumnWidthStyleStrategy columnWidthStyleStrategy) throws Exception {
		setHead(response, excelName);
		//更新Class注解值
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		logger.info("开始更新Class注解值国际化");
		updateClassExcelPropertyValue(clazz);
		stopWatch.stop();
		logger.info("结束更新Class注解值国际化,耗时:{}秒",stopWatch.getTotalTimeSeconds());
		//开始导出
		stopWatch = new StopWatch();
		stopWatch.start();
		logger.info("开始导出Excel");
		FastExcel.write(response.getOutputStream(), clazz)
				.registerWriteHandler(ValidateUtils.getOrDefault(cellStyleStrategy,formatExcel()))
				.registerWriteHandler(ValidateUtils.getOrDefault(columnWidthStyleStrategy,new ExcelWidthStyleStrategy()))
				.sheet(0,ValidateUtils.getOrDefault(sheetName,"sheet"))
				.doWrite(data);
		stopWatch.stop();
		logger.info("结束导出Excel,耗时:{}秒",stopWatch.getTotalTimeSeconds());
	}

	/**
	 * 导出
	 * @param excelName 文件名需要带上后缀名
	 * @param sheetName 表名 不填默认为sheet
	 * @param clazz     需要导出excel的类,其中ExcelProperty注解国际化是类名+.+字段名组成
	 * @param data      数据
	 */
	public static void export(HttpServletResponse response, String excelName, String sheetName,Class<?> clazz,List<?> data) throws Exception {
		export(response, excelName, sheetName, clazz, data, null, null);
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
	public static MultipartFile export(String excelName, String sheetName, Class<?> clazz,List<?> data,HorizontalCellStyleStrategy cellStyleStrategy,AbstractColumnWidthStyleStrategy columnWidthStyleStrategy) throws Exception {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
			//更新Class注解值
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("开始更新Class注解值国际化");
			updateClassExcelPropertyValue(clazz);
			stopWatch.stop();
			logger.info("结束更新Class注解值国际化,耗时:{}秒",stopWatch.getTotalTimeSeconds());
			//开始导出
			stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("开始导出Excel");
			ExcelWriterBuilder excelWriterBuilder = FastExcel.write(os, clazz).registerWriteHandler(ValidateUtils.getOrDefault(cellStyleStrategy,formatExcel())).registerWriteHandler(ValidateUtils.getOrDefault(columnWidthStyleStrategy,new ExcelWidthStyleStrategy()));
			ExcelWriter excelWriter = excelWriterBuilder.build();
			ExcelWriterSheetBuilder excelWriterSheetBuilder;
			WriteSheet writeSheet;
			excelWriterSheetBuilder = new ExcelWriterSheetBuilder(excelWriter);
			excelWriterSheetBuilder.sheetNo(0).sheetName(sheetName);
			writeSheet = excelWriterSheetBuilder.build();
			excelWriter.write(data, writeSheet);
			// 必须要finish才会写入，不finish只会创建empty的文件
			excelWriter.finish();
			stopWatch.stop();
			logger.info("结束导出Excel,耗时:{}秒",stopWatch.getTotalTimeSeconds());
			byte[] content = os.toByteArray();
			//生成文件
			try(InputStream is = new ByteArrayInputStream(content);){
				return new MockMultipartFile(excelName,excelName, MediaType.MULTIPART_FORM_DATA_VALUE, is);
			}
		}
	}

	/**
	 * 导出
	 * @param excelName 文件名需要带上后缀名
	 * @param sheetName 表名 不填默认为sheet
	 * @param clazz     需要导出excel的类,其中ExcelProperty注解国际化是类名+.+字段名组成
	 * @param data      数据
	 * @return          文件
	 */
	public static MultipartFile export(String excelName, String sheetName, Class<?> clazz,List<?> data) throws Exception {
		return export(excelName, sheetName, clazz, data, null, null);
	}

	private static void updateClassExcelPropertyValue(Class<?> clazz) throws Exception {
		for(Field field : clazz.getDeclaredFields()){
			String i18nKey = clazz.getSimpleName()+"."+field.getName();
			Map<String,Object> annotationNameAndValueMap = new HashMap<>();
			annotationNameAndValueMap.put("value", I18nUtils.getMessage(i18nKey,(String)null));
			ClassUtils.updateClassField(field, ExcelProperty.class,annotationNameAndValueMap);
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
		response.setCharacterEncoding(BaseCode.UTF8);
		// 这里URLEncoder.encode可以防止中文乱码
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
	}

	private static HorizontalCellStyleStrategy formatExcel() {
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
}
