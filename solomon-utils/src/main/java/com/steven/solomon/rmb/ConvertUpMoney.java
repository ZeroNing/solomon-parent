package com.steven.solomon.rmb;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.verification.ValidateUtils;

/**
 * 人民币金额大写转换工具。
 *
 * <p>保留历史输出格式，只优化校验、字符串构建和注释可读性。</p>
 */
public final class ConvertUpMoney {

  /**
   * 大写数字。
   */
  private static final String[] NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

  /**
   * 整数部分单位，最大支持到万亿级。
   */
  private static final String[] INTEGER_UNITS = {
      "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万", "拾", "佰", "仟"
  };

  /**
   * 小数部分单位：角、分、厘。
   */
  private static final String[] DECIMAL_UNITS = {"角", "分", "厘"};

  private static final String NUMBER_PATTERN = "(-)?\\d*(\\.\\d*)?";

  private ConvertUpMoney() {}

  public static String toChineseNum(String str) {
    return toChinese(str) + "整    ￥" + str + "/";
  }

  public static String toChina(String str) {
    return toChinese(str) + "整";
  }

  /**
   * 将数字金额转换成中文大写金额。
   */
  private static String toChinese(String str) {
    if (ValidateUtils.isEmpty(str) || !str.matches(NUMBER_PATTERN)) {
      return str;
    }
    if ("0".equals(str) || "0.00".equals(str) || "0.0".equals(str)) {
      return "零元";
    }

    boolean negative = str.startsWith("-");
    String amount = removeSignAndSeparator(str);
    String integerStr = resolveIntegerPart(amount);
    String decimalStr = resolveDecimalPart(amount);
    if (integerStr.length() > INTEGER_UNITS.length || hasInvalidLeadingZero(integerStr)) {
      return negative ? "-" + amount : amount;
    }

    String result =
        getChineseInteger(toIntArray(integerStr), isReachWanUnit(integerStr))
            + getChineseDecimal(toIntArray(decimalStr));
    return negative ? "负" + result : result;
  }

  /**
   * 将数字字符串转为 int 数组，便于按位匹配单位。
   */
  private static int[] toIntArray(String number) {
    int[] array = new int[number.length()];
    for (int i = 0; i < number.length(); i++) {
      array[i] = number.charAt(i) - '0';
    }
    return array;
  }

  /**
   * 将整数部分转换为大写金额。
   */
  public static String getChineseInteger(int[] integers, boolean isWan) {
    StringBuilder chineseInteger = new StringBuilder();
    int length = integers.length;
    if (length == 1 && integers[0] == 0) {
      return StrUtil.EMPTY;
    }
    for (int i = 0; i < length; i++) {
      chineseInteger.append(resolveIntegerText(integers, length, i, isWan));
    }
    return chineseInteger.toString();
  }

  /**
   * 将小数部分转换为大写金额，最多保留到厘。
   */
  private static String getChineseDecimal(int[] decimals) {
    StringBuilder chineseDecimal = new StringBuilder();
    int length = Math.min(decimals.length, DECIMAL_UNITS.length);
    for (int i = 0; i < length; i++) {
      if (decimals[i] != 0) {
        chineseDecimal.append(NUMBERS[decimals[i]]).append(DECIMAL_UNITS[i]);
      }
    }
    return chineseDecimal.toString();
  }

  private static String resolveIntegerText(int[] integers, int length, int index, boolean isWan) {
    int value = integers[index];
    if (value != 0) {
      return NUMBERS[value] + INTEGER_UNITS[length - index - 1];
    }
    String key = resolveZeroUnit(integers, length, index, isWan);
    if ((length - index) > 1 && integers[index + 1] != 0) {
      key += NUMBERS[0];
    }
    return key;
  }

  private static String resolveZeroUnit(int[] integers, int length, int index, boolean isWan) {
    int leftLength = length - index;
    if (leftLength == 13) {
      return INTEGER_UNITS[4];
    }
    if (leftLength == 9) {
      return INTEGER_UNITS[8];
    }
    if (leftLength == 5 && isWan) {
      return INTEGER_UNITS[4];
    }
    if (leftLength == 1) {
      return INTEGER_UNITS[0];
    }
    return StrUtil.EMPTY;
  }

  private static boolean isReachWanUnit(String integerStr) {
    int length = integerStr.length();
    if (length <= 4) {
      return false;
    }
    String subInteger = length > 8
        ? integerStr.substring(length - 8, length - 4)
        : integerStr.substring(0, length - 4);
    return Integer.parseInt(subInteger) > 0;
  }

  private static String removeSignAndSeparator(String str) {
    return str.replace("-", StrUtil.EMPTY).replace(",", StrUtil.EMPTY);
  }

  private static String resolveIntegerPart(String amount) {
    int pointIndex = amount.indexOf(".");
    if (pointIndex > 0) {
      return amount.substring(0, pointIndex);
    }
    return pointIndex == 0 ? StrUtil.EMPTY : amount;
  }

  private static String resolveDecimalPart(String amount) {
    int pointIndex = amount.indexOf(".");
    return pointIndex >= 0 ? amount.substring(pointIndex + 1) : StrUtil.EMPTY;
  }

  private static boolean hasInvalidLeadingZero(String integerStr) {
    int[] integers = toIntArray(integerStr);
    return integers.length > 1 && integers[0] == 0;
  }
}
