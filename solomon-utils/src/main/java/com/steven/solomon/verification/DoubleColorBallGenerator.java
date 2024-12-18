package com.steven.solomon.verification;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoubleColorBallGenerator {

    private static final int RED_BALL_COUNT = 6; // 红球数量
    private static final int BLUE_BALL_COUNT = 1; // 蓝球数量
    private static final int MAX_RED_BALL_NUMBER = 33; // 红球最大号码
    private static final int MAX_BLUE_BALL_NUMBER = 16; // 蓝球最大号码

    public static List<Integer> generateRedBalls(Integer ballSize) throws InterruptedException {
        List<Integer> redBalls = new ArrayList<>();
        for (int i = 1; i <= MAX_RED_BALL_NUMBER; i++) {
            Thread.sleep(28L);
            redBalls.add(i);
        }
        Collections.shuffle(redBalls); // 随机排序
        return redBalls.subList(0, ValidateUtils.getOrDefault(ballSize,RED_BALL_COUNT)); // 取前6个
    }

    public static List<Integer> generateBlueBall(Integer ballSize) throws InterruptedException {
        Thread.sleep(28L);
        List<Integer> result = new ArrayList<>();
        ballSize = ValidateUtils.getOrDefault(ballSize,BLUE_BALL_COUNT);
        while (result.size() < (ballSize)) {
            Integer blue = (int) (Math.random() * MAX_BLUE_BALL_NUMBER) + 1;
            if(!result.contains(blue)){
                result.add(blue);
            }
        }
        return result; // 随机生成1-16的整数
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 1; i <= 5; i++) {
            System.out.print("第" + i + "组号码: " + StrUtil.EMPTY);
            List<Integer> redBalls = generateRedBalls(8);
            List<Integer> blueBall = generateBlueBall(4);
            System.out.print("红球：");
            for (int ball : redBalls) {
                System.out.print(ball + " ");
            }
            System.out.println("蓝球：" + blueBall);
        }
    }

}
