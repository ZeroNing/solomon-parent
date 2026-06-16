package com.steven.solomon.utils.excel;

import cn.hutool.core.util.ObjectUtil;

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
import com.steven.solomon.utils.excel.converter.ListExcelConverter;
import com.steven.solomon.utils.excel.handler.ImageCellWriteHandler;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
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
 * Excel 工具类。
 *
 * <p>统一处理导出响应头、表头国际化、默认样式和图片/集合转换器注册。</p>
 */
public final class ExcelUtils {

	private static final Logger logger = LoggerUtils.logger(ExcelUtils.class);

	private ExcelUtils() {}

	/**
	 * 导出
	 * @param excelName 文件名需要带上后缀名
	 * @param sheetName 表名 不填默认为sheet
	 * @param clazz     需要导出excel的类,其中ExcelProperty注解国际化是类名+.+字段名组成
	 * @param data      数据
	 */
	public static void export(HttpServletResponse response, String excelName, String sheetName, Class<?> clazz, List<?> data, HorizontalCellStyleStrategy cellStyleStrategy, AbstractColumnWidthStyleStrategy columnWidthStyleStrategy) throws Exception {
		setHead(response, excelName);
		updateExcelHeaderI18n(clazz);
		StopWatch stopWatch = startLog("开始导出Excel");
		FastExcel.write(response.getOutputStream(), clazz)
				.registerConverter(new ListExcelConverter()).registerWriteHandler(new ImageCellWriteHandler())
				.registerWriteHandler(ObjectUtil.defaultIfNull(cellStyleStrategy,formatExcel()))
				.registerWriteHandler(ObjectUtil.defaultIfNull(columnWidthStyleStrategy,new ExcelWidthStyleStrategy()))
				.sheet(0,ObjectUtil.defaultIfNull(sheetName,"sheet"))
				.doWrite(data);
		stopLog(stopWatch, "结束导出Excel");
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
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			updateExcelHeaderI18n(clazz);
			StopWatch stopWatch = startLog("开始导出Excel");
			ExcelWriter excelWriter = buildWriter(os, clazz, cellStyleStrategy, columnWidthStyleStrategy);
			try {
				excelWriter.write(data, buildSheet(excelWriter, sheetName));
			} finally {
				// 必须 finish 才会写入内容；放在 finally 中避免异常时资源未释放。
				excelWriter.finish();
			}
			stopLog(stopWatch, "结束导出Excel");
			byte[] content = os.toByteArray();
			// 生成内存文件，便于后续复用上传或转发逻辑。
			try (InputStream is = new ByteArrayInputStream(content)) {
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

	/**
	 * 更新 Excel 表头国际化文案，并记录耗时。
	 */
	private static void updateExcelHeaderI18n(Class<?> clazz) throws Exception {
		StopWatch stopWatch = startLog("开始更新Class注解值国际化");
		updateClassExcelPropertyValue(clazz);
		stopLog(stopWatch, "结束更新Class注解值国际化");
	}

	private static void updateClassExcelPropertyValue(Class<?> clazz) throws Exception {
		for (Field field : clazz.getDeclaredFields()) {
			String i18nKey = clazz.getSimpleName()+"."+field.getName();
			Map<String,Object> annotationNameAndValueMap = new HashMap<>();
			String value = I18nUtils.getMessage(i18nKey,(String)null);
			if (ObjectUtil.isNotEmpty(value)) {
				annotationNameAndValueMap.put("value", value);
				ClassUtils.updateClassField(field, ExcelProperty.class,annotationNameAndValueMap);
			}
		}
	}

	private static ExcelWriter buildWriter(ByteArrayOutputStream os, Class<?> clazz, HorizontalCellStyleStrategy cellStyleStrategy, AbstractColumnWidthStyleStrategy columnWidthStyleStrategy) {
		ExcelWriterBuilder excelWriterBuilder = FastExcel.write(os, clazz)
				.registerConverter(new ListExcelConverter())
				.registerWriteHandler(new ImageCellWriteHandler())
				.registerWriteHandler(ObjectUtil.defaultIfNull(cellStyleStrategy,formatExcel()))
				.registerWriteHandler(ObjectUtil.defaultIfNull(columnWidthStyleStrategy,new ExcelWidthStyleStrategy()));
		return excelWriterBuilder.build();
	}

	private static WriteSheet buildSheet(ExcelWriter excelWriter, String sheetName) {
		ExcelWriterSheetBuilder excelWriterSheetBuilder = new ExcelWriterSheetBuilder(excelWriter);
		excelWriterSheetBuilder.sheetNo(0).sheetName(ObjectUtil.defaultIfNull(sheetName, "sheet"));
		return excelWriterSheetBuilder.build();
	}

	private static StopWatch startLog(String message) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		logger.info(message);
		return stopWatch;
	}

	private static void stopLog(StopWatch stopWatch, String message) {
		stopWatch.stop();
		logger.info("{},耗时:{}秒", message, stopWatch.getTotalTimeSeconds());
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
