package com.steven.solomon.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 双色球号码生成工具。
 *
 * <p>该工具只负责生成不重复的随机号码，不包含任何业务投注逻辑。
 * 生成过程不做 sleep，保证工具方法可以在接口或批处理场景中安全复用。</p>
 */
public class DoubleColorBallGenerator {

  private static final int RED_BALL_COUNT = 6;
  private static final int BLUE_BALL_COUNT = 1;
  private static final int MAX_RED_BALL_NUMBER = 33;
  private static final int MAX_BLUE_BALL_NUMBER = 16;

  private DoubleColorBallGenerator() {
  }

  /**
   * 生成红球号码。
   *
   * @param ballSize 需要生成的数量，为空时默认 6 个
   * @return 随机红球号码列表
   */
  public static List<Integer> generateRedBalls(Integer ballSize) {
    return randomNumbers(MAX_RED_BALL_NUMBER, ValidateUtils.getOrDefault(ballSize, RED_BALL_COUNT));
  }

  /**
   * 生成蓝球号码。
   *
   * @param ballSize 需要生成的数量，为空时默认 1 个
   * @return 随机蓝球号码列表
   */
  public static List<Integer> generateBlueBall(Integer ballSize) {
    return randomNumbers(MAX_BLUE_BALL_NUMBER,
        ValidateUtils.getOrDefault(ballSize, BLUE_BALL_COUNT));
  }

  private static List<Integer> randomNumbers(int maxNumber, int size) {
    int limit = Math.max(0, Math.min(size, maxNumber));
    List<Integer> numbers = new ArrayList<>(maxNumber);
    for (int i = 1; i <= maxNumber; i++) {
      numbers.add(i);
    }
    Collections.shuffle(numbers);
    return new ArrayList<>(numbers.subList(0, limit));
  }
}
