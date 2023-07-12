package com.steven.solomon.utils.date;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Set;

public class DateTimeUtils {

	public static final DateTimeFormatter TIME_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final DateTimeFormatter DATE_FORMATTER      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter DATE_FORMATTER_YEAR = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	public static final DateTimeFormatter DATETIME_FORMATTER  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter YEAR_FORMATTER      = DateTimeFormatter.ofPattern("yyyy");
	public static final DateTimeFormatter MONTH_FORMATTER     = DateTimeFormatter.ofPattern("MM");

	/**
	 * 获取LocalTime所有时区
	 * @return
	 */
	public static Set<String> getZoneIds(){
		 return ZoneId.getAvailableZoneIds();
	}

	/**
	 * 获取Localtime时区
	 */
	public static ZoneId getZoneId(String zoneId){
		return ZoneId.of(zoneId);
	}

	/**
	 * 获取当前系统时间
	 *
	 * @return
	 */
	public static LocalTime getLocalTime(String zoneId) {
		return LocalTime.now(getZoneId(zoneId));
	}

	/**
	 * 获取当前系统时间
	 *
	 * @return
	 */
	public static LocalTime getLocalTime(ZoneId zoneId) {
		return LocalTime.now(zoneId);
	}


	/**
	 * 获取当前系统时间
	 * 
	 * @return
	 */
	public static LocalTime getLocalTime() {
		return LocalTime.now();
	}

	/**
	 * 获取当前系统日期
	 *
	 * @return
	 */
	public static LocalDate getLocalDate(String zoneId) {
		return LocalDate.now(getZoneId(zoneId));
	}

	/**
	 * 获取当前系统日期
	 *
	 * @return
	 */
	public static LocalDate getLocalDate(ZoneId zoneId) {
		return LocalDate.now(zoneId);
	}

	/**
	 * 获取当前系统日期
	 * 
	 * @return
	 */
	public static LocalDate getLocalDate() {
		return LocalDate.now();
	}

	/**
	 * 获取当前系统日期时间
	 *
	 * @return
	 */
	public static LocalDateTime getLocalDateTime(String zoneId) {
		return LocalDateTime.now(getZoneId(zoneId));
	}

	/**
	 * 获取当前系统日期时间
	 *
	 * @return
	 */
	public static LocalDateTime getLocalDateTime(ZoneId zoneId) {
		return LocalDateTime.now(zoneId);
	}

	/**
	 * 获取当前系统日期时间
	 * 
	 * @return
	 */
	public static LocalDateTime getLocalDateTime() {
		return LocalDateTime.now();
	}

	/**
	 * 获取当前系统时间字符串
	 * 
	 * @return
	 */
	public static String getLocalTimeString() {
		return LocalTime.now().format(TIME_FORMATTER);
	}

	/**
	 * 获取当前系统时间字符串
	 *
	 * @return
	 */
	public static String getLocalTimeString(LocalTime time,DateTimeFormatter formatter) {
		return time.format(formatter);
	}

	/**
	 * 获取当前系统日期字符串
	 * 
	 * @return
	 */
	public static String getLocalDateString() {
		return LocalDate.now().format(DATE_FORMATTER);
	}

	/**
	 * 获取当前系统日期字符串
	 *
	 * @return
	 */
	public static String getLocalDateString(LocalDate local,DateTimeFormatter formatter) {
		return local.format(formatter);
	}

	/**
	 * 获取当前系统日期字符串
	 * 
	 * @return
	 */
	public static String getLocalTimeString(DateTimeFormatter dateTimeFormatter) {
		return LocalTime.now().format(dateTimeFormatter);
	}

	/**
	 * 获取当前系统日期时间字符串
	 * 
	 * @return
	 */
	public static String getLocalDateTimeString() {
		return LocalDateTime.now().format(DATETIME_FORMATTER);
	}

	/**
	 * 获取当前系统日期时间字符串
	 *
	 * @return
	 */
	public static String getLocalDateTimeString(LocalDateTime time,DateTimeFormatter dateTimeFormatter) {
		return time.format(dateTimeFormatter);
	}

	/**
	 * 获取当前系统日期时间字符串
	 * 
	 * @return
	 */
	public static String getLocalDateTimeString(DateTimeFormatter dateTimeFormatter) {
		return LocalDateTime.now().format(dateTimeFormatter);
	}

	/**
	 * 获取当前系统年份
	 * 
	 * @return
	 */
	public static String getLocalYearString() {
		return LocalDateTime.now().format(YEAR_FORMATTER);
	}

	/**
	 * 获取当前系统月份
	 * 
	 * @return
	 */
	public static String getLocalMonthString() {
		return LocalDateTime.now().format(MONTH_FORMATTER);
	}

	/**
	 * 字符串转LocalTime
	 * 
	 * @param time
	 * @return
	 */
	public static LocalTime string2LocalTime(String time) {
		return LocalTime.parse(time, TIME_FORMATTER);
	}

	/**
	 * 字符串转LocalTime
	 * 
	 * @param time
	 * @return
	 */
	public static LocalTime string2LocalTime(String time, DateTimeFormatter dateTimeFormatter) {
		return LocalTime.parse(time, dateTimeFormatter);
	}

	/**
	 * 字符串转LocalDate
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDate string2LocalDate(String date) {
		return LocalDate.parse(date, DATE_FORMATTER);
	}

	/**
	 * 字符串转LocalDateTime
	 * 
	 * @param dateTime
	 * @return
	 */
	public static LocalDateTime string2LocalDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, DATETIME_FORMATTER);
	}

	/**
	 * 字符串转LocalDateTime
	 *
	 * @param dateTime
	 * @return
	 */
	public static LocalDateTime string2LocalDateTime(String dateTime,DateTimeFormatter dateTimeFormatter) {
		return LocalDateTime.parse(dateTime, dateTimeFormatter);
	}

	/**
	 * 字符串转LocalDate
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDate string2LocalDate(String date, DateTimeFormatter dateTimeFormatter) {
		return LocalDate.parse(date, dateTimeFormatter);
	}

	/**
	 * Date转LocalDateTime
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDateTime date2LocalDateTime(Date date) {
		// An instantaneous point on the time-line.(时间线上的一个瞬时点。)
		Instant instant = date.toInstant();
		// A time-zone ID, such as {@code Europe/Paris}.(时区)
		ZoneId zoneId = ZoneId.systemDefault();
		LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
		return localDateTime;
	}

	/**
	 * Date转LocalDate
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDate date2LocalDate(Date date) {
		// An instantaneous point on the time-line.(时间线上的一个瞬时点。)
		Instant instant = date.toInstant();
		// A time-zone ID, such as {@code Europe/Paris}.(时区)
		ZoneId zoneId = ZoneId.systemDefault();
		LocalDate localDate = instant.atZone(zoneId).toLocalDate();
		return localDate;
	}

	public static Date localDate2Date(LocalDate localDate) {
		if(localDate == null) {
			return null;
		}
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zonedDateTime.toInstant());
	}

	/**
	 * Date转LocalDate
	 * 
	 * @param date
	 * @return
	 */
	public static LocalTime date2LocalTime(Date date) {
		// An instantaneous point on the time-line.(时间线上的一个瞬时点。)
		Instant instant = date.toInstant();
		// A time-zone ID, such as {@code Europe/Paris}.(时区)
		ZoneId zoneId = ZoneId.systemDefault();
		LocalTime localTime = instant.atZone(zoneId).toLocalTime();
		return localTime;
	}

	/**
	 * LocalDateTime转换为Date
	 * 
	 * @param localDateTime
	 */
	public static Date localDateTime2Date(LocalDateTime localDateTime) {
		ZoneId zoneId = ZoneId.systemDefault();
		// Combines this date-time with a time-zone to create a
		ZonedDateTime zdt = localDateTime.atZone(zoneId);
															// ZonedDateTime.
		Date date = Date.from(zdt.toInstant());
		return date;
	}

	/**
	 * 获取时间差
	 * 
	 * @param minMillisecond 最小毫秒数
	 * @param maxMillisecond 最大毫秒数
	 * @return
	 */
	public static Duration remainDuration(Long minMillisecond, Long maxMillisecond) {
		LocalDateTime  minLocal = date2LocalDateTime(new Date(minMillisecond));
		LocalDateTime  maxLocal = date2LocalDateTime(new Date(maxMillisecond));
		Duration duration = Duration.between(minLocal, maxLocal);
		return duration;
	}

	/**
	 * 获取相差多少分钟
	 *
	 * @param minMillisecond
	 * @param maxMillisecond
	 * @return
	 */
	public static Long remainMinute(Long minMillisecond, Long maxMillisecond) {
		Duration duration = remainDuration(minMillisecond, maxMillisecond);
		return duration.toMinutes();
	}

	/**
	 * 获取相差多少天
	 *
	 * @param minMillisecond
	 * @param maxMillisecond
	 * @return
	 */
	public static Long remainDays(Long minMillisecond, Long maxMillisecond) {
		Duration duration = remainDuration(minMillisecond, maxMillisecond);
		return duration.toDays();
	}

	/**
	 * 获取相差多少小时
	 *
	 * @param minMillisecond
	 * @param maxMillisecond
	 * @return
	 */
	public static Long remainHours(Long minMillisecond, Long maxMillisecond) {
		Duration duration = remainDuration(minMillisecond, maxMillisecond);
		return duration.toHours();
	}

	/**
	 * 获取某天0点0分0秒
	 * @return
	 */
	public static LocalDateTime getNowZeroDayTime(Date date){
		LocalDateTime nowZeroDayTime = LocalDateTime.of(date2LocalDate(date),LocalTime.MIN);
		return nowZeroDayTime;
	}

	/**
	 * 获取某天0点0分0秒
	 * @return
	 */
	public static Date getNowZeroDayDate(Date date){
		return localDateTime2Date(getNowZeroDayTime(date));
	}

	/**
	 * 获取某天0点0分0秒字符串
	 * @return
	 */
	public static String getNowZeroDayTimeStr(Date date,DateTimeFormatter dateTimeFormatter){
		return dateTimeFormatter.format(getNowZeroDayTime(date));
	}

	/**
	 * 获取某天23点59分59秒
	 * @return
	 */
	public static LocalDateTime getNowMaxDayTime(Date date){
		LocalDateTime nowZeroDayTime = LocalDateTime.of(date2LocalDate(date),LocalTime.MAX);
		return nowZeroDayTime;
	}

	/**
	 * 获取某天23点59分59秒
	 * @return
	 */
	public static Date getNowMaxDayDate(Date date){
		return localDateTime2Date(getNowMaxDayTime(date));
	}

	/**
	 * 获取某天23点59分59秒
	 * @return
	 */
	public static String getNowMaxDayTime(Date date,DateTimeFormatter dateTimeFormatter){
		return dateTimeFormatter.format(getNowMaxDayTime(date));
	}

	/**
	 * 获取当月第一天
	 * @return
	 */
	public static LocalDate getNowMonthFirstDayTime(){
		LocalDateTime firstday = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
		return firstday.toLocalDate();
	}

	/**
	 * 获取当月第一天字符串
	 * @return
	 */
	public static String getNowMonthFirstDayTimeStr(DateTimeFormatter dateTimeFormatter){
		return dateTimeFormatter.format(getNowMonthFirstDayTime());
	}

	/**
	 * 获取当月最后一天
	 * @return
	 */
	public static LocalDate getNowMonthLaseDayTime(){
		LocalDateTime lastDay = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());
		return lastDay.toLocalDate();
	}

	/**
	 * 获取当月第一天字符串
	 * @return
	 */
	public static String getNowMonthLaseDayTimeStr(DateTimeFormatter dateTimeFormatter){
		return dateTimeFormatter.format(getNowMonthLaseDayTime());
	}

	/**
	 * 减少天数/月份/年份
	 * @param number 减少数量
	 * @param chronoUnit 减少年还是月还是天
	 * @return
	 */
	public static LocalDate minus(int number, ChronoUnit chronoUnit){
		LocalDate localDate = getLocalDate();
		return minus(localDate,number,chronoUnit);
	}

	/**
	 * 减少天数/月份/年份
	 * @param number 新增数量
	 * @param chronoUnit 减少年还是月还是天
	 * @return
	 */
	public static LocalDate minus(LocalDate localDate,int number, ChronoUnit chronoUnit){
		localDate = localDate.minus(number,chronoUnit);
		return localDate;
	}

	public static Date minus(Date date,int number, ChronoUnit chronoUnit){
		LocalDate localDate = date2LocalDate(date);
		return localDate2Date(minus(localDate,number,chronoUnit));
	}

	/**
	 * 新增天数/月份/年份
	 * @param number 新增数量
	 * @param chronoUnit 减少年还是月还是天
	 * @return
	 */
	public static LocalDate add(int number, ChronoUnit chronoUnit){
		LocalDate localDate = getLocalDate();
		return add(localDate,number,chronoUnit);
	}

	/**
	 * 新增天数/月份/年份
	 * @param number 新增数量
	 * @param chronoUnit 减少年还是月还是天
	 * @return
	 */
	public static LocalDate add(LocalDate localDate,int number, ChronoUnit chronoUnit){
		localDate = localDate.plus(number,chronoUnit);
		return localDate;
	}

	public static Date add(Date date,int number, ChronoUnit chronoUnit){
		LocalDateTime localDate = date2LocalDateTime(date);
		return localDateTime2Date(add(localDate,number,chronoUnit));
	}

	public static LocalDateTime add(LocalDateTime localDate,int number, ChronoUnit chronoUnit){
		localDate = localDate.plus(number,chronoUnit);
		return localDate;
	}

	/**
	 * 判断传入的时间是否在当前时间之前
	 * @param dateTime
	 * @return
	 */
	public static boolean isBefore(LocalDateTime dateTime){
		return dateTime.isBefore(LocalDateTime.now());
	}

	/**
	 * 判断传入的时间是否在当前时间之后
	 * @param dateTime
	 * @return
	 */
	public static boolean isAfter(LocalDateTime dateTime){
		return dateTime.isAfter(LocalDateTime.now());
	}

	/**
	 * 时区转换
	 *
	 * @param localDateTime
	 * @param originZoneId
	 * @param targetZoneId
	 * @return
	 */
	public static LocalDateTime convertLocalDateTime(LocalDateTime localDateTime, ZoneId originZoneId,
			ZoneId targetZoneId) {
		return localDateTime.atZone(originZoneId).withZoneSameInstant(targetZoneId).toLocalDateTime();
	}
}
