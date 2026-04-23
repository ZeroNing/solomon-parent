package com.steven.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.consumer.AbstractJobConsumer;
import com.steven.solomon.enums.ScheduleTypeEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@JobTask
public class TestHandler2 extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }

    public static void main(String[] args) {
        // 1. 配置文件读取器：设置分隔符为竖线
        CsvReadConfig config = new CsvReadConfig();
        config.setFieldSeparator('|');

        // 2. 指定 CSV 文件路径（请根据实际情况修改）
        String filePath = "C:/Users/黄/Downloads/Sample file.csv";  // 替换为你的文件路径
        File csvFile = FileUtil.file(filePath);

        // 用于存储标题头
        AtomicReference<List<String>> headersRef = new AtomicReference<>();
        List<Map<String, String>> dataMaps = new ArrayList<>();
        CsvData csvData = CsvUtil.getReader(config).read(csvFile, CharsetUtil.CHARSET_UTF_8);
        List<CsvRow> rows = csvData.getRows();
        for (CsvRow row: rows) {
            if (headersRef.get() == null) {
                // 第一行：保存标题头
                headersRef.set(row.getRawList());
            } else {
                // 数据行：构建 Map
                List<String> values = row.getRawList();
                List<String> headers = headersRef.get();
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size() && i < values.size(); i++) {
                    map.put(headers.get(i), values.get(i));
                }
                dataMaps.add(map);
            }
        }
        System.out.println(JSONUtil.toJsonStr(dataMaps.get(0)));
    }
}
