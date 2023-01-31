package com.steven.solomon.utils.ip;

import com.steven.solomon.logger.LoggerUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

public class IPAddressUitls {

//	private final static String TAO_BAO_URL = "http://ip.taobao.com/service/getIpInfo.php?ip=";

	private static final Logger logger = LoggerUtils.logger(IPAddressUitls.class);
	/**
	 * 功能：判断IPv4地址的正则表达式：
	 */
	private static final Pattern IPV4_REGEX = Pattern
			.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	/**
	 * 功能：判断标准IPv6地址的正则表达式
	 */
	private static final Pattern IPV6_STD_REGEX = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

	/**
	 *  功能：判断一般情况压缩的IPv6正则表达式
	 */
	private static final Pattern IPV6_COMPRESS_REGEX = Pattern
			.compile("^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$");

	/**
	 * 由于IPv6压缩规则是必须要大于等于2个全0块才能压缩 不合法压缩 ： fe80:0000:8030:49ec:1fc6:57fa:ab52:fe69
	 * -> fe80::8030:49ec:1fc6:57fa:ab52:fe69 该不合法压缩地址直接压缩了处于第二个块的单独的一个全0块，
	 * 上述不合法地址不能通过一般情况的压缩正则表达式IPV6_COMPRESS_REGEX判断出其不合法 所以定义了如下专用于判断边界特殊压缩的正则表达式
	 * (边界特殊压缩：开头或末尾为两个全0块，该压缩由于处于边界，且只压缩了2个全0块，不会导致':'数量变少)
	 * 功能：抽取特殊的边界压缩情况
	 */
	private static final Pattern IPV6_COMPRESS_REGEX_BORDER = Pattern.compile(
			"^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$");

	/**
	 * 判断是否为合法IPv4地址
	 */
	public static boolean isIpv4Address(final String input) {
		return IPV4_REGEX.matcher(input).matches();
	}

	/**
	 * 判断是否为合法IPv6地址
	 */
	public static boolean isIpv6Address(final String ip) {
		int num = 0;
		String inputStr = null;
		if (StringUtils.contains(ip, "%")) {
			inputStr = ip.substring(0, ip.indexOf("%"));
		} else {
			inputStr = ip;
		}
		for (int i = 0; i < inputStr.length(); i++) {
			if (inputStr.charAt(i) == ':') {
				num++;
			}
		}
		if (num > 7) {
			return false;
		}
		if (IPV6_STD_REGEX.matcher(inputStr).matches()) {
			return true;
		}
		if (num == 7) {
			return IPV6_COMPRESS_REGEX_BORDER.matcher(inputStr).matches();
		} else {
			return IPV6_COMPRESS_REGEX.matcher(inputStr).matches();
		}
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
	 * <p>
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
	 * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
	 * <p>
	 * 如：X-Forwarded-For：10.160.70.178,192.168.1.110, 192.168.1.120, 192.168.1.130,
	 * 192.168.1.100
	 * <p>
	 * 用户真实IP为： 192.168.1.110
	 *
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {

		Enumeration<?> headerNames = request.getHeaderNames();
		//
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			logger.info("------nextElement ={} ,\n ------------request.getHeader(nextElement) ={}", key, value);
		}
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("x-real-ip");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// 返回多个ip 处理
		if (StringUtils.contains(ip, ",")) {
			logger.info("返回IP多条 ip ={}", ip);
			String[] addres = ip.split(",");
			for (int i = 0; i < addres.length; i++) {
				if (!addres[i].trim().startsWith("10.") && !addres[i].trim().startsWith("100.")
						&& !addres[i].trim().startsWith("192.") && !("127.0.0.1").equals(addres[i].trim())) {
					logger.info("过滤后只返回公网IP：{}", addres[i].trim());
					ip = addres[i].trim();
					break;
				}
			}
		}
		logger.info("-------getIpAddress----返回 ip ={}", ip);
		return ip;
	}

	/**
	 * 获取二进制字节
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1) {
			return null;
		}
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

//	/**
//	 * 通过IP获取地址(需要联网，调用淘宝的IP库)
//	 *
//	 * @param ip ip
//	 * @return 地址
//	 */
//	public static String getIpInfo(final String ip) {
//		String info = "";
//		try {
//			final URL url = new URL(TAO_BAO_URL + ip);
//			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setRequestMethod("GET");
//			connection.setDoOutput(true);
//			connection.setDoInput(true);
//			connection.setUseCaches(false);
//
//			final InputStream in = connection.getInputStream();
//			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
//			final StringBuilder temp = new StringBuilder();
//			String line = bufferedReader.readLine();
//			while (line != null) {
//				temp.append(line).append("\r\n");
//				line = bufferedReader.readLine();
//			}
//			bufferedReader.close();
//			final JSONObject obj = (JSONObject) JSON.parse(temp.toString());
//			if (obj.getIntValue("code") == 0) {
//				final JSONObject data = obj.getJSONObject("data");
//				info += data.getString("country") + " ";
//				info += data.getString("region") + " ";
//				info += data.getString("city") + " ";
//				info += data.getString("isp");
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
//		return info;
//	}

	/**
	 * 将字符串形式的ip地址转换为BigInteger
	 * 
	 * @param ipInString 字符串形式的ip地址
	 * @return 整数形式的ip地址
	 */
	public static BigInteger stringToBigInt(String ipInString) {
		ipInString = ipInString.replace(" ", "");
		byte[] bytes;
		if (ipInString.contains(":")) {
			bytes = ipv6ToBytes(ipInString);
		} else {
			bytes = ipv4ToBytes(ipInString);
		}
		return new BigInteger(bytes);
	}

	/**
	 * 将整数形式的ip地址转换为字符串形式
	 * 
	 * @param ipInBigInt 整数形式的ip地址
	 * @return 字符串形式的ip地址
	 */
	public static String bigIntToString(BigInteger ipInBigInt) {
		byte[] bytes = ipInBigInt.toByteArray();
		byte[] unsignedBytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		if (bytes.length == 4 || bytes.length == 16) {
			unsignedBytes = bytes;
		}
		// 去除符号位
		try {
			String ip = InetAddress.getByAddress(unsignedBytes).toString();
			return ip.substring(ip.indexOf('/') + 1).trim();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ipv6地址转有符号byte[17]
	 */
	private static byte[] ipv6ToBytes(String ipv6) {
		byte[] ret = new byte[17];
		ret[0] = 0;
		int ib = 16;
		// ipv4混合模式标记
		boolean comFlag = false;
		// 去掉开头的冒号
		if (ipv6.startsWith(":")) {
			ipv6 = ipv6.substring(1);
		}
		String groups[] = ipv6.split(":");
		// 反向扫描
		for (int ig = groups.length - 1; ig > -1; ig--) {
			if (groups[ig].contains(".")) {
				// 出现ipv4混合模式
				byte[] temp = ipv4ToBytes(groups[ig]);
				ret[ib--] = temp[4];
				ret[ib--] = temp[3];
				ret[ib--] = temp[2];
				ret[ib--] = temp[1];
				comFlag = true;
			} else if ("".equals(groups[ig])) {
				// 出现零长度压缩,计算缺少的组数
				int zlg = 9 - (groups.length + (comFlag ? 1 : 0));
				// 将这些组置0
				while (zlg-- > 0) {
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
	 * ipv4地址转有符号byte[5]
	 */
	private static byte[] ipv4ToBytes(String ipv4) {
		byte[] ret = new byte[5];
		ret[0] = 0;
		// 先找到IP地址字符串中.的位置
		int position1 = ipv4.indexOf(".");
		int position2 = ipv4.indexOf(".", position1 + 1);
		int position3 = ipv4.indexOf(".", position2 + 1);
		// 将每个.之间的字符串转换成整型
		ret[1] = (byte) Integer.parseInt(ipv4.substring(0, position1));
		ret[2] = (byte) Integer.parseInt(ipv4.substring(position1 + 1, position2));
		ret[3] = (byte) Integer.parseInt(ipv4.substring(position2 + 1, position3));
		ret[4] = (byte) Integer.parseInt(ipv4.substring(position3 + 1));
		return ret;
	}

}
