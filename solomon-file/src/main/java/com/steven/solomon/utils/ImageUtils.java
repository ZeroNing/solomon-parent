package com.steven.solomon.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Positions;

/**
 * 图片处理工具类
 */
public class ImageUtils {

  /**
   * 对图片进行指定比例的压缩
   * @param originalInputStream 原图字节输入流
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param outputQuality 图片质量 1是最高清 0是最差的
   */
  public static BufferedImage compress(InputStream originalInputStream ,float scale,float outputQuality) throws Exception {
    return Thumbnails.of(originalInputStream).scale(scale).outputQuality(outputQuality).asBufferedImage();

  }

  /**
   * 对图片按照宽高进行压缩
   * @param originalInputStream 原图字节输入流
   * @param width 宽度
   * @param height 长度
   * @param outputQuality 图片质量 1是最高清 0是最差的
   */
  public static BufferedImage compressSize(InputStream originalInputStream,int width,int height,float outputQuality) throws Exception {
    return Thumbnails.of(originalInputStream).size(width,height).outputQuality(outputQuality).asBufferedImage();

  }

  /**
   * 旋转
   * @param originalInputStream 原图字节输入流
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param rotate 旋转度数
   * @throws IOException
   */
  public final static BufferedImage rotate(InputStream originalInputStream,float scale,double rotate) throws Exception {
    return Thumbnails.of(originalInputStream).scale(scale).rotate(rotate).outputQuality(1).asBufferedImage();

  }

  /**
   * 图片格式转换
   * @param originalInputStream 原图字节输入流
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param outputFormat 转换格式
   */
  public final static BufferedImage conversion(InputStream originalInputStream,float scale,String outputFormat) throws Exception {
    return Thumbnails.of(originalInputStream).scale(scale).outputFormat(outputFormat).asBufferedImage();
  }

  /**
   * 裁剪图片
   * @param originalInputStream 原图字节输入流
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param x 裁切图片的X轴
   * @param y 裁切图片的Y轴
   * @param width 裁切图片的宽度
   * @param height 裁切图片的高度
   * @throws IOException
   */
  public final static BufferedImage tailoring(InputStream originalInputStream,float scale,int x,int y,int width,int height) throws Exception {
    return Thumbnails.of(originalInputStream).scale(scale).sourceRegion(x,y,width,height).outputQuality(1).asBufferedImage();
  }

  /**
   * 添加水印
   * @param originalInputStream 原图字节输入流
   * @param watermarkIntInputStream 水印图字节输入流
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param positions 水印位置
   * @param transparency 透明度
   * @param watermarkSize 水印图片大小占原图的多少
   */
  public final static BufferedImage watermark(InputStream originalInputStream, InputStream watermarkIntInputStream,float scale, Positions positions,float transparency,double watermarkSize)
      throws Exception {
    BufferedImage bufferedImage = compressWatermark(originalInputStream,watermarkIntInputStream,watermarkSize);
    return Thumbnails.of(originalInputStream).scale(scale).watermark(positions,bufferedImage,transparency).outputQuality(1).asBufferedImage();
  }

  /**
   * 添加水印
   * @param originalInputStream 原图字节输入流
   * @param watermarkIntInputStream 水印图字节输入流
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param x 水印放在原图的X轴
   * @param y 水印放在原图的Y轴
   * @param transparency 透明度
   * @param watermarkSize 水印图片大小占原图的多少
   * @throws IOException
   */
  public final static BufferedImage watermark(InputStream originalInputStream, InputStream watermarkIntInputStream,float scale,int x,int y,float transparency,double watermarkSize)
      throws Exception {
    BufferedImage bufferedImage = compressWatermark(originalInputStream,watermarkIntInputStream,watermarkSize);
    return Thumbnails.of(watermarkIntInputStream).scale(scale).watermark(new Coordinate(x,y),bufferedImage,transparency).outputQuality(1).asBufferedImage();
  }

  /**
   * 添加水印
   * @param filePath 图片绝对源路径
   * @param watermarkPath 水印图片路径
   * @param scale 图片大小 1是原图大小 0.5是原图的一半相当于长宽一半
   * @param x 水印放在原图的X轴
   * @param y 水印放在原图的Y轴
   * @param width 水印宽
   * @param height 水印高
   * @param transparency 透明度
   * @throws IOException
   */
  public final static BufferedImage watermark(String filePath,String watermarkPath,float scale,int x,int y,int width,int height,float transparency)
      throws Exception {
    BufferedImage bufferedImage = Thumbnails.of(watermarkPath).size(width,height).keepAspectRatio(false).asBufferedImage();
    return Thumbnails.of(filePath).scale(scale).watermark(new Coordinate(x,y),bufferedImage,transparency).outputQuality(1).asBufferedImage();
  }

  /**
   * 压缩水印图宽高
   * @param originalInputStream 原图字节输入流
   * @param watermarkIntInputStream 水印图字节输入流
   * @param watermarkSize 水印图片大小占原图的多少
   * @return 返回压缩后的水印图
   * @throws IOException
   */
  private static BufferedImage compressWatermark(InputStream originalInputStream, InputStream watermarkIntInputStream,
      double watermarkSize) throws IOException {
    // 读取原图，获取宽高
    BufferedImage image = ImageIO.read(originalInputStream);
    // 根据比例计算新的水印图宽高
    int width = (int) (image.getWidth() * watermarkSize);
    int height = width * image.getHeight() / image.getWidth();

    BufferedImage bufferedImage = Thumbnails.of(watermarkIntInputStream).size(width,height)
        .keepAspectRatio(false).asBufferedImage();
    return bufferedImage;
  }

}
