package com.network_monitor.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "snmp_data")
public class SnmpData {

  @Id
  private String id;

  @Field("oid")
  private String oid;

  @Field("value")
  private String value;

  @Field("device_ip")
  private String deviceIp;

  @Field("timestamp")
  private LocalDateTime timestamp;

  @Field("metric_type")
  private String metricType;

  @Field("readable_value")
  private String readableValue;

  @Field("created_at")
  private LocalDateTime createdAt;

  @Field("frequency_type")
  private String frequencyType;

  // Default constructor
  public SnmpData() {
    this.createdAt = LocalDateTime.now();
  }

  // Constructor with all fields
  public SnmpData(String oid, String value, String deviceIp, LocalDateTime timestamp, String metricType,
      String frequencyType) {
    this.oid = oid;
    this.value = value;
    this.deviceIp = deviceIp;
    this.timestamp = timestamp;
    this.metricType = metricType;
    this.frequencyType = frequencyType;
    this.createdAt = LocalDateTime.now();
  }

  // Setter for created_at (if needed for manual setting)
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

}
