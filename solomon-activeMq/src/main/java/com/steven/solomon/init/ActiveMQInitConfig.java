package com.steven.solomon.init;

import com.steven.solomon.annotation.ActiveMQ;
import com.steven.solomon.spring.SpringUtil;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn("springUtil")
public class ActiveMQInitConfig implements CommandLineRunner {

  private ActiveMQ activeMQ;

  @Override
  public void run(String... args) throws Exception {
//    List<Object> list = SpringUtil.getBeansWithAnnotation(ActiveMQ.class).values();
  }
}
