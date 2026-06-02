package com.steven.solomon.graphics2D;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.graphics2D.entity.BaseReceipt;
import com.steven.solomon.graphics2D.entity.FileUpload;
import com.steven.solomon.service.FileServiceInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/**
 * 电子票据绘制抽象服务。
 *
 * <p>提供票据图片绘制的骨架流程，包括创建画布、设置背景色、
 * 绘制表格线、填写描述和数据、上传等步骤。
 * 子类通过实现抽象方法定制具体票据的绘制逻辑。</p>
 *
 * @param <T> 票据实体类型，需继承 {@link BaseReceipt}
 */
public abstract class AbsReceiptService<T extends BaseReceipt>{

  /** 每行高度（像素）。 */
  protected Integer rowHeight;
  /** 上方留白像素。 */
  protected Integer startHeight;
  /** 左侧留白像素。 */
  protected Integer startWidth;
  /** 图片高度（像素）。 */
  protected Integer imageHeight;
  /** 图片宽度（像素）。 */
  protected Integer imageWidth;

  /**
   * 绘制票据（使用默认白色背景和 RGB 颜色模式）。
   *
   * @param receipt 票据数据
   * @return 上传后的文件信息
   * @throws Exception 绘制或上传失败时抛出
   */
  public FileUpload drawReceipt(T receipt) throws Exception {
    return drawReceipt(BufferedImage.TYPE_INT_RGB, Color.white,receipt);
  }

  /**
   * 绘制票据（自定义背景色和颜色模式）。
   *
   * @param imageType      图片类型，如 {@link BufferedImage#TYPE_INT_RGB}
   * @param backgroundColor 背景颜色
   * @param receipt        票据数据
   * @return 上传后的文件信息
   * @throws Exception 绘制或上传失败时抛出
   */
  public FileUpload drawReceipt(int imageType,Color backgroundColor, T receipt) throws Exception{
    BufferedImage bufferedImage = null;
    try {
      bufferedImage = drawImageBasics(imageType);
      Graphics2D g2 = drawBackgroundColor(bufferedImage,backgroundColor);
      g2 = drawTableLines(g2);
      g2 = drawDescribe(g2,receipt);
      g2 = drawData(g2,receipt);
      g2 = setGraphics2DOptimize(g2);
      return upload(bufferedImage,receipt);
    } finally {
      if (ObjectUtil.isNotEmpty(bufferedImage)) {
        bufferedImage.getGraphics().dispose();
        bufferedImage= null;
      }
      System.gc();
    }

  }

  /**
   * 设置 Graphics2D 抗锯齿和画笔柔顺优化。
   *
   * <p>2D 画图绘制字体时会产生锯齿，通过开启抗锯齿和文字抗锯齿
   * 以及设置画笔样式来提升图像质量。</p>
   */
  public Graphics2D setGraphics2DOptimize(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    Stroke s = new BasicStroke(imageWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    g2.setStroke(s);
    return g2;
  }

  /**
   * 绘制图片基本画布。
   *
   * @param imageType 图片类型
   * @return 创建的画布对象
   */
  public BufferedImage drawImageBasics(int imageType) {
    return new BufferedImage(imageWidth,imageHeight,imageType);
  }

  /**
   * 绘制图片背景颜色。
   *
   * @param bufferedImage 画布对象
   * @param color         背景颜色
   * @return Graphics2D 绘制上下文
   */
  public Graphics2D drawBackgroundColor(BufferedImage bufferedImage, Color color) {
    Graphics2D g2 =bufferedImage.createGraphics();
    //设置颜色
    g2.setColor(color);
    //填充整张图片(其实就是设置背景颜色)
    g2.fillRect(0,0,bufferedImage.getWidth(),bufferedImage.getHeight());
    return g2;
  }

  /**
   * 绘制表格线条
   */
  public abstract Graphics2D drawTableLines(Graphics2D g2);

  /**
   * 绘制表格文字描述
   */
  public abstract Graphics2D drawDescribe(Graphics2D g2,T receipt);

  /**
   * 绘制动态数据
   */
  public abstract Graphics2D drawData(Graphics2D g2, T receipt);

  /**
   * 上传票据图片。
   *
   * @param bufferedImage 票据图片
   * @param receipt       票据数据
   * @return 上传后的文件信息
   * @throws Exception 上传失败时抛出
   */
  public abstract FileUpload upload(BufferedImage bufferedImage,T receipt) throws Exception;
}
