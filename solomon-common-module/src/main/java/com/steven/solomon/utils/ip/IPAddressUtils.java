package com.steven.solomon.utils.ip;

import cn.hutool.core.util.StrUtil;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类。
 *
 * <p>提供客户端真实 IP 提取、IPv4/IPv6 校验，以及 IP 与整数之间的互转。</p>
 */
public final class IPAddressUtils {

  private static final String UNKNOWN = "unknown";

  private static final String[] CLIENT_IP_HEADERS = {
      "x-forwarded-for",
      "x-real-ip",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_CLIENT_IP",
      "HTTP_X_FORWARDED_FOR"
  };

  /**
   * IPv4 正则。原实现 `{3}` 前存在空格，会导致合法 IPv4 无法匹配。
   */
  private static final Pattern IPV4_REGEX =
      Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

  /**
   * 标准 IPv6 正则。
   */
  private static final Pattern IPV6_STD_REGEX =
      Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

  /**
   * 一般压缩 IPv6 正则。
   */
  private static final Pattern IPV6_COMPRESS_REGEX =
      Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$");

  /**
   * 边界压缩 IPv6 正则，用于补充识别开头或末尾压缩的特殊情况。
   */
  private static final Pattern IPV6_COMPRESS_REGEX_BORDER =
      Pattern.compile("^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$");

  private IPAddressUtils() {}

  /**
   * 判断是否为合法 IPv4 地址。
   */
  public static boolean isIpv4Address(final String input) {
    return StrUtil.isNotBlank(input) && IPV4_REGEX.matcher(input).matches();
  }

  /**
   * 判断是否为合法 IPv6 地址。
   */
  public static boolean isIpv6Address(final String ip) {
    if (StrUtil.isBlank(ip)) {
      return false;
    }
    String input = StrUtil.contains(ip, "%") ? ip.substring(0, ip.indexOf("%")) : ip;
    int colonCount = countColon(input);
    if (colonCount > 7) {
      return false;
    }
    if (IPV6_STD_REGEX.matcher(input).matches()) {
      return true;
    }
    return colonCount == 7
        ? IPV6_COMPRESS_REGEX_BORDER.matcher(input).matches()
        : IPV6_COMPRESS_REGEX.matcher(input).matches();
  }

  /**
   * 获取客户端真实 IP。
   *
   * <p>按常见代理头顺序查找；当 X-Forwarded-For 存在多个值时，优先返回第一个公网 IP。</p>
   */
  public static String getIpAddress(HttpServletRequest request) {
    String ip = firstValidHeaderIp(request);
    if (isInvalidIp(ip)) {
      ip = request.getRemoteAddr();
    }
    return choosePublicIp(ip);
  }

  /**
   * 将十六进制字符串转换为字节数组。
   */
  public static byte[] parseHexStr2Byte(String hexStr) {
    if (StrUtil.isBlank(hexStr)) {
      return null;
    }
    byte[] result = new byte[hexStr.length() / 2];
    for (int i = 0; i < result.length; i++) {
      int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
      int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
      result[i] = (byte) (high * 16 + low);
    }
    return result;
  }

  /**
   * 将字符串形式的 IP 地址转换为整数。
   */
  public static BigInteger stringToBigInt(String ipInString) {
    String ip = ipInString.replace(" ", StrUtil.EMPTY);
    byte[] bytes = ip.contains(":") ? ipv6ToBytes(ip) : ipv4ToBytes(ip);
    return new BigInteger(bytes);
  }

  /**
   * 将整数形式的 IP 地址转换为字符串。
   */
  public static String bigIntToString(BigInteger ipInBigInt) {
    byte[] bytes = ipInBigInt.toByteArray();
    byte[] unsignedBytes = Arrays.copyOfRange(bytes, 1, bytes.length);
    if (bytes.length == 4 || bytes.length == 16) {
      unsignedBytes = bytes;
    }
    try {
      String ip = InetAddress.getByAddress(unsignedBytes).toString();
      return ip.substring(ip.indexOf('/') + 1).trim();
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("非法 IP 整数: " + ipInBigInt, e);
    }
  }

  private static String firstValidHeaderIp(HttpServletRequest request) {
    for (String header : CLIENT_IP_HEADERS) {
      String ip = request.getHeader(header);
      if (!isInvalidIp(ip)) {
        return ip;
      }
    }
    return null;
  }

  private static boolean isInvalidIp(String ip) {
    return StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip);
  }

  private static String choosePublicIp(String ip) {
    if (!StrUtil.contains(ip, ",")) {
      return ip;
    }
    for (String address : ip.split(",")) {
      String candidate = address.trim();
      if (!isPrivateIpv4(candidate)) {
        return candidate;
      }
    }
    return ip.split(",")[0].trim();
  }

  private static boolean isPrivateIpv4(String ip) {
    return ip.startsWith("10.")
        || ip.startsWith("100.")
        || ip.startsWith("192.")
        || "127.0.0.1".equals(ip);
  }

  private static int countColon(String input) {
    int count = 0;
    for (int i = 0; i < input.length(); i++) {
      if (input.charAt(i) == ':') {
        count++;
      }
    }
    return count;
  }

  /**
   * IPv6 地址转有符号 byte[17]，首位补 0 避免 BigInteger 当成负数。
   */
  private static byte[] ipv6ToBytes(String ipv6) {
    byte[] ret = new byte[17];
    ret[0] = 0;
    int ib = 16;
    boolean mixedIpv4 = false;
    if (ipv6.startsWith(":")) {
      ipv6 = ipv6.substring(1);
    }
    String[] groups = ipv6.split(":");
    for (int ig = groups.length - 1; ig > -1; ig--) {
      if (groups[ig].contains(".")) {
        byte[] temp = ipv4ToBytes(groups[ig]);
        ret[ib--] = temp[4];
        ret[ib--] = temp[3];
        ret[ib--] = temp[2];
        ret[ib--] = temp[1];
        mixedIpv4 = true;
      } else if (StrUtil.EMPTY.equals(groups[ig])) {
        int zeroLengthGroups = 9 - (groups.length + (mixedIpv4 ? 1 : 0));
        while (zeroLengthGroups-- > 0) {
          ret[ib--] = 0;
          ret[ib--] = 0;
        }
      } else {
        int temp = Integer.parseInt(groups[ig], 16);
        ret[ib--] = (byte) temp;
        ret[ib--] = (byte) (temp >> 8);
      }
    }
    return ret;
  }

  /**
   * IPv4 地址转有符号 byte[5]，首位补 0 避免 BigInteger 当成负数。
   */
  private static byte[] ipv4ToBytes(String ipv4) {
    String[] segments = ipv4.split("\\.");
    byte[] ret = new byte[5];
    ret[0] = 0;
    for (int i = 0; i < segments.length; i++) {
      ret[i + 1] = (byte) Integer.parseInt(segments[i]);
    }
    return ret;
  }
}
