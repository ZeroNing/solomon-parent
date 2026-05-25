package com.steven.solomon.clamav.utils;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.clamav.properties.ClamAvProperties;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

/**
 * ClamAV 病毒扫描工具。
 *
 * <p>封装 ClamAV 客户端扫描结果，统一返回是否命中病毒，方便上传文件前做安全校验。</p>
 */
public class ClamAvUtils {

  private static final String SCAN_FILE_ERROR = "SCAN_FILE_ERROR";

  private final Logger logger = LoggerUtils.logger(ClamAvUtils.class);

  private final ClamavClient client;

  private final ClamAvProperties properties;

  public ClamAvUtils(ClamavClient client, ClamAvProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  /**
   * 扫描文件是否存在病毒。
   *
   * @param inputStream 文件流
   * @return true 表示发现病毒；false 表示未发现或未启用扫描
   */
  public boolean scanFile(InputStream inputStream) throws BaseException {
    if (!properties.getEnabled()) {
      return false;
    }
    if (ValidateUtils.isEmpty(inputStream)) {
      throw new IllegalArgumentException("Input stream is empty");
    }
    return hasVirus(client.scan(inputStream));
  }

  /**
   * 扫描文件，发现病毒时抛出指定业务异常。
   *
   * @param inputStream 文件流
   * @param errorCode 发现病毒时使用的异常编码
   */
  public void scanFile(InputStream inputStream, String errorCode) throws BaseException {
    if (scanFile(inputStream)) {
      throw new BaseException(errorCode);
    }
  }

  private boolean hasVirus(ScanResult scanResult) throws BaseException {
    if (scanResult instanceof ScanResult.OK) {
      return false;
    }
    if (scanResult instanceof ScanResult.VirusFound virusFound) {
      logFoundViruses(virusFound.getFoundViruses());
      return true;
    }
    throw new BaseException(SCAN_FILE_ERROR);
  }

  private void logFoundViruses(Map<String, Collection<String>> foundViruses) {
    logger.info("扫描文件出现高风险病毒:{}", JSONUtil.toJsonStr(foundViruses));
  }
}
