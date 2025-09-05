package com.steven.solomon.clamav.utils;

import com.steven.solomon.clamav.properties.ClamAvProperties;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class ClamAvUtils {

    private final Logger logger = LoggerUtils.logger(ClamAvUtils.class);

    private final ClamavClient client;

    private final ClamAvProperties properties;

    public ClamAvUtils(ClamavClient client, ClamAvProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    /**
     * 扫描文件是否有病毒
     * @param inputStream 文件流
     * @return true 有病毒 false 没有病毒
     * @throws BaseException
     */
    public boolean scanFile(InputStream inputStream) throws BaseException {
        if(!properties.getEnabled()){
            return false;
        }
        if(ValidateUtils.isEmpty(inputStream)){
            throw new IllegalArgumentException("Input stream is empty");
        }
        ScanResult scanResult = client.scan(inputStream);
        if(scanResult instanceof ScanResult.OK) {
           return false;
        }
        if(scanResult instanceof ScanResult.VirusFound) {
            Map<String, Collection<String>> foundViruses = ((ScanResult.VirusFound) scanResult).getFoundViruses();
            logger.info(foundViruses.toString());
            return true;
        }
        throw new BaseException("ERROR_CODE_SCAN_FILE_ERROR");
    }

    /**
     * 扫描文件是否有病毒
     * @param inputStream 文件流
     * @param errorCode 有病毒异常编码
     * @throws BaseException
     */
    public void scanFile(InputStream inputStream,String errorCode) throws BaseException {
        if(!scanFile(inputStream)){
            throw new BaseException(errorCode);
        }
    }
}
