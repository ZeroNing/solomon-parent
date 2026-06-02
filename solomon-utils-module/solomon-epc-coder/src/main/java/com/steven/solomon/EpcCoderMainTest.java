package com.steven.solomon;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;
import com.steven.solomon.model.Gs1BarcodeResult;
import com.steven.solomon.service.EpcService;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;

/**
 * EPC 编解码 Main 方法测试类。
 * <p>
 * 提供 SGTIN-96、SGTIN-198、GIAI-96、GIAI-202 等多种 EPC 类型的
 * GS1 译码、EPC 编码生成和 EPC 反译测试示例。
 * </p>
 *
 * @author 创建者
 */
public class EpcCoderMainTest {

  /** 日志记录器。 */
  private static final Logger LOGGER = LoggerUtils.logger(EpcCoderMainTest.class);

  /**
   * 运行全部 EPC 编解码示例。
   *
   * @param args 启动参数
   * @throws BaseException 编解码异常
   */
  public static void main(String[] args) throws BaseException {
    EpcService epcService = new EpcService();

    testSgtin96(epcService);
  }

  /**
   * 测试 SGTIN-96 的 GS1 译码、EPC 生成和 EPC 反译。
   *
   * @param epcService EPC 服务
   * @throws BaseException 编解码异常
   */
  private static void testSgtin96(EpcService epcService) throws BaseException {
    String ai01 = "06901234567892";
    String ai21 = "1234567890";
    Gs1BarcodeResult gs1Result = epcService.parseGs1Barcode("(01)" + ai01 + "(21)" + ai21);
    EpcResult encodeResult = epcService.gs1()
            .ai01(ai01)
            .ai21(ai21)
            .companyPrefixLength(6)
            .tagSize(96)
            .encode();
    printGs1("SGTIN-96 GS1译码", gs1Result);
    LOGGER.info("SGTIN-96 EPC生成标签: {}", encodeResult.getHex());
    LOGGER.info("SGTIN-96 EPC反译: {}", JSONUtil.toJsonStr(epcService.decodeEpc(encodeResult.getHex())));
  }

  /**
   * 测试 SGTIN-198 的 GS1 译码、EPC 生成和 EPC 反译。
   *
   * @param epcService EPC 服务
   * @throws BaseException 编解码异常
   */
  private static void testSgtin198(EpcService epcService) throws BaseException {
    String ai01 = "06901234567892";
    String ai21 = "ABC123";
    Gs1BarcodeResult gs1Result = epcService.parseGs1Barcode("(01)" + ai01 + "(21)" + ai21);
    EpcResult encodeResult = epcService.gs1()
            .ai01(ai01)
            .ai21(ai21)
            .companyPrefixLength(6)
            .tagSize(198)
            .encode();
    EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());

    printGs1("SGTIN-198 GS1译码", gs1Result);
    printEpc("SGTIN-198 EPC生成", encodeResult);
    printEpc("SGTIN-198 EPC反译", decodeResult);
  }

  /**
   * 测试 GIAI-96 的 GS1 译码、EPC 生成和 EPC 反译。
   *
   * @param epcService EPC 服务
   * @throws BaseException 编解码异常
   */
  private static void testGiai96(EpcService epcService) throws BaseException {
    String ai8004 = "690123123456";
    Gs1BarcodeResult gs1Result = epcService.parseGs1Barcode("(8004)" + ai8004);
    EpcResult encodeResult = epcService.gs1()
            .ai8004(ai8004)
            .companyPrefixLength(6)
            .tagSize(96)
            .encode();
    EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());

    printGs1("GIAI-96 GS1译码", gs1Result);
    printEpc("GIAI-96 EPC生成", encodeResult);
    printEpc("GIAI-96 EPC反译", decodeResult);
  }

  /**
   * 测试 GIAI-202 的 GS1 译码、EPC 生成和 EPC 反译。
   *
   * @param epcService EPC 服务
   * @throws BaseException 编解码异常
   */
  private static void testGiai202(EpcService epcService) throws BaseException {
    String ai8004 = "690123ASSET001";
    Gs1BarcodeResult gs1Result = epcService.parseGs1Barcode("(8004)" + ai8004);
    EpcResult encodeResult = epcService.gs1()
            .ai8004(ai8004)
            .companyPrefixLength(6)
            .tagSize(202)
            .encode();
    EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());

    printGs1("GIAI-202 GS1译码", gs1Result);
    printEpc("GIAI-202 EPC生成", encodeResult);
    printEpc("GIAI-202 EPC反译", decodeResult);
  }

  /**
   * 打印 GS1 译码结果。
   *
   * @param title 输出标题
   * @param result GS1 译码结果
   */
  private static void printGs1(String title, Gs1BarcodeResult result) {
    LOGGER.info("========== {} ==========", title);
    LOGGER.info("content={}", result.getContent());
    LOGGER.info("AI 01={}", result.getAi01());
    LOGGER.info("AI 21={}", result.getAi21());
    LOGGER.info("AI 8004={}", result.getAi8004());
  }

  /**
   * 打印 EPC 编码或反译结果。
   *
   * @param title 输出标题
   * @param result EPC 结果
   */
  private static void printEpc(String title, EpcResult result) {
    LOGGER.info("========== {} ==========", title);
    LOGGER.info("type={}", result.getType());
    LOGGER.info("bitLength={}", result.getBitLength());
    LOGGER.info("hex={}", result.getHex());
    LOGGER.info("uri={}", result.getUri());
    LOGGER.info("AI 01={}", result.getAi01());
    LOGGER.info("AI 21={}", result.getAi21());
    LOGGER.info("AI 8004={}", result.getAi8004());
    LOGGER.info("companyPrefix={}", result.getCompanyPrefix());
    LOGGER.info("itemReference={}", result.getItemReference());
    LOGGER.info("assetReference={}", result.getAssetReference());
    LOGGER.info("serial={}", result.getSerial());
  }
}
