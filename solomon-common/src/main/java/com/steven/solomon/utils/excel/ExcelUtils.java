package com.steven.solomon.utils.excel;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.steven.solomon.clazz.ClassUtils;
import com.steven.solomon.constant.code.BaseCode;
import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.Charsets;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel?????????
 */
public class ExcelUtils {

	private static final Logger logger = LoggerUtils.logger(ExcelUtils.class);

	/**
	 * ??????Excel(07???.xlsx)???web
	 *
	 * @param response  ??????
	 * @param excelName Excel??????
	 * @param sheetName sheet?????????
	 * @param clazz     Excel??????????????????
	 * @param data      ??????????????????
	 * @throws Exception
	 */
	public static MultipartFile exportUpload(HttpServletResponse response, String excelName, String sheetName,List<String> includeColumnFieldNames,Map<String,String> excelPropertyValueMap, Class<?> clazz,List<?> data) throws Exception {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
			setHead(response, excelName);
			ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(os, clazz).registerWriteHandler(formatExcel()).registerWriteHandler(new ExcelWidthStyleStrategy());

			if(ValidateUtils.isNotEmpty(includeColumnFieldNames)){
				excelWriterBuilder.includeColumnFiledNames(includeColumnFieldNames);
				for(String filedName : includeColumnFieldNames){
					String i18nName = excelPropertyValueMap.get(filedName);
					if(ValidateUtils.isEmpty(i18nName)){
						continue;
					}
					Map<String,Object> annotationNameAndValueMap = new HashMap<>();
					annotationNameAndValueMap.put("value",i18nName);
					ClassUtils.updateClassField(clazz,filedName,ExcelProperty.class,annotationNameAndValueMap);
				}
			}

			ExcelWriter excelWriter = excelWriterBuilder.build();
			ExcelWriterSheetBuilder excelWriterSheetBuilder;
			WriteSheet              writeSheet;
			excelWriterSheetBuilder = new ExcelWriterSheetBuilder(excelWriter);
			excelWriterSheetBuilder.sheetNo(0).sheetName(sheetName);
			writeSheet = excelWriterSheetBuilder.build();
			excelWriter.write(data, writeSheet);
			// ?????????finish??????????????????finish????????????empty?????????
			excelWriter.finish();
			byte[] content = os.toByteArray();
			try(InputStream is = new ByteArrayInputStream(content);){
				return new MockMultipartFile(excelName,excelName, MediaType.MULTIPART_FORM_DATA_VALUE, is);
			}
		}
	}

	/**
	 * ???????????????
	 *
	 * @param response ?????????????????????
	 * @param fileName ????????????
	 */
	private static void setHead(HttpServletResponse response, String fileName) {

		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		// ??????URLEncoder.encode????????????????????????
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
	}

	public static HorizontalCellStyleStrategy formatExcel() {
		WriteCellStyle headWriteCellStyle = new WriteCellStyle();
		headWriteCellStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
		WriteFont headWriteFont = new WriteFont();
		headWriteFont.setFontHeightInPoints((short) 10);
		headWriteCellStyle.setWriteFont(headWriteFont);
		// ???????????????
		WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
		WriteFont      contentWriteFont      = new WriteFont();
		// ????????????
		contentWriteFont.setFontHeightInPoints((short) 10);
		contentWriteCellStyle.setWriteFont(contentWriteFont);
		// ??????????????????
		contentWriteCellStyle.setWrapped(false);
		// ??????????????????
		contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// ??????????????????
		contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

		return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

	}

	/**
	 * ???????????????????????????
	 */
	public static class ExcelWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

		@Override
		protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> list, Cell cell, Head head,
				Integer integer, Boolean aBoolean) {
			// ????????????
			Sheet sheet = writeSheetHolder.getSheet();
			sheet.setColumnWidth(cell.getColumnIndex(), 5000);
		}
	}

}
