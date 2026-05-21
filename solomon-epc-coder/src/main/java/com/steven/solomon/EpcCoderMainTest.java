package com.steven.solomon;

import com.steven.solomon.exception.BaseException;
import com.steven.solomon.model.EpcResult;
import com.steven.solomon.model.Gs1BarcodeResult;
import com.steven.solomon.service.EpcService;

/**
 * EPC 编解码 main 方法测试。
 */
public class EpcCoderMainTest {

  /**
   * 运行全部 EPC 编解码示例。
   *
   * @param args 启动参数
   * @throws BaseException 编解码异常
   */
  public static void main(String[] args) throws BaseException {
    EpcService epcService = new EpcService();

    testSgtin96(epcService);
    testSgtin198(epcService);
    testGiai96(epcService);
    testGiai202(epcService);
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
    EpcResult decodeResult = epcService.decodeEpc(encodeResult.getHex());

    printGs1("SGTIN-96 GS1译码", gs1Result);
    printEpc("SGTIN-96 EPC生成", encodeResult);
    printEpc("SGTIN-96 EPC反译", decodeResult);
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
    System.out.println("\n========== " + title + " ==========");
    System.out.println("content=" + result.getContent());
    System.out.println("AI 01=" + result.getAi01());
    System.out.println("AI 21=" + result.getAi21());
    System.out.println("AI 8004=" + result.getAi8004());
  }

  /**
   * 打印 EPC 编码或反译结果。
   *
   * @param title 输出标题
   * @param result EPC 结果
   */
  private static void printEpc(String title, EpcResult result) {
    System.out.println("\n========== " + title + " ==========");
    System.out.println("type=" + result.getType());
    System.out.println("bitLength=" + result.getBitLength());
    System.out.println("hex=" + result.getHex());
    System.out.println("uri=" + result.getUri());
    System.out.println("AI 01=" + result.getAi01());
    System.out.println("AI 21=" + result.getAi21());
    System.out.println("AI 8004=" + result.getAi8004());
    System.out.println("companyPrefix=" + result.getCompanyPrefix());
    System.out.println("itemReference=" + result.getItemReference());
    System.out.println("assetReference=" + result.getAssetReference());
    System.out.println("serial=" + result.getSerial());
  }
}
