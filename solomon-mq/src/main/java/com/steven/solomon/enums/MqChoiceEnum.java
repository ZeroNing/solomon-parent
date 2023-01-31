package com.steven.solomon.enums;

import org.omg.PortableInterceptor.ACTIVE;

public enum MqChoiceEnum implements BaseEnum{
  RABBIT("RabbitMq","RabbitMq"),
  ACTIVE("ActiveMQ","ActiveMQ"),
  KAFKA("kafka","kafka"),;

  private final String label;

  private final String description;

  MqChoiceEnum(String label,String description) {
    this.label = label;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String label() {
    return this.label;
  }

  @Override
  public String key() {
    return this.name();
  }

  public String getLabel() {
    return label;
  }
}
