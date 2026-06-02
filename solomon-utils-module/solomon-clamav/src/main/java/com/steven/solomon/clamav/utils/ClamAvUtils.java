package com.steven.solomon.clamav.utils;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.clamav.properties.ClamAvProperties;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

/**
 * ClamAV 病毒扫描工具类。
 * <p>
 * 封装了 ClamAV 客户端的文件扫描功能，提供便捷的病毒检测方法。
 * 支持快速扫描（返回是否有病毒）和抛异常式扫描（检测到病毒时抛出业务异常）。
 * </p>
 *
 * @author 创建者
 */
public class ClamAvUtils {

  /** 文件扫描异常错误码。 */
  private static final String SCAN_FILE_ERROR = "SCAN_FILE_ERROR";

  /** 日志记录器。 */
  private final Logger logger = LoggerUtils.logger(ClamAvUtils.class);

  /** ClamAV 客户端。 */
  private final ClamavClient client;

  /** ClamAV 配置属性。 */
  private final ClamAvProperties properties;

  /**
   * 创建 ClamAV 扫描工具实例。
   *
   * @param client     ClamAV 客户端（可能为 null，当未启用时）
   * @param properties ClamAV 配置属性
   */
  public ClamAvUtils(ClamavClient client, ClamAvProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  /**
   * 扫描文件输入流，检测是否包含病毒。
   * <p>
   * 如果 ClamAV 未启用则直接返回 {@code false}。
   * </p>
   *
   * @param inputStream 文件的输入流
   * @return 如果检测到病毒则返回 {@code true}，否则返回 {@code false}
   * @throws BaseException 文件扫描过程发生异常时抛出
   * @throws IllegalArgumentException 输入流为空时抛出
   */
  public boolean scanFile(InputStream inputStream) throws BaseException {
    if (!properties.getEnabled()) { return false; }
    if (ObjectUtil.isEmpty(inputStream)) { throw new IllegalArgumentException("Input stream is empty"); }
    return hasVirus(client.scan(inputStream));
  }

  /**
   * 扫描文件输入流，检测到病毒时抛出指定错误码的业务异常。
   * <p>
   * 适用于需要中断业务流程的场景，例如文件上传时检测到病毒直接抛异常。
   * </p>
   *
   * @param inputStream 文件的输入流
   * @param errorCode   检测到病毒时抛出的错误码
   * @throws BaseException 检测到病毒或扫描异常时抛出
   */
  public void scanFile(InputStream inputStream, String errorCode) throws BaseException {
    if (scanFile(inputStream)) { throw new BaseException(errorCode); }
  }

  /**
   * 判断扫描结果是否包含病毒。
   *
   * @param scanResult ClamAV 扫描结果
   * @return 如果发现病毒则返回 {@code true}
   * @throws BaseException 扫描结果无法识别时抛出
   */
  private boolean hasVirus(ScanResult scanResult) throws BaseException {
    if (scanResult instanceof ScanResult.OK) { return false; }
    if (scanResult instanceof ScanResult.VirusFound virusFound) { logFoundViruses(virusFound.getFoundViruses()); return true; }
    throw new BaseException(SCAN_FILE_ERROR);
  }

  /**
   * 记录发现的病毒信息到日志。
   *
   * @param foundViruses 发现的病毒映射表（文件名 → 病毒名称集合）
   */
  private void logFoundViruses(Map<String, Collection<String>> foundViruses) {
    logger.info("扫描文件出现高风险病毒:{}", JSONUtil.toJsonStr(foundViruses));
  }
}
