package com.network_monitor.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.network_monitor.model.SnmpData;

@Repository
public interface SnmpDataRepository extends MongoRepository<SnmpData, String> {

  // Device IP'ye göre veri bulma
  List<SnmpData> findByDeviceIp(String deviceIp);

  // Metric type'a göre veri bulma
  List<SnmpData> findByMetricType(String metricType);

  // Device IP ve metric type'a göre veri bulma
  List<SnmpData> findByDeviceIpAndMetricType(String deviceIp, String metricType);

  // Belirli bir tarih aralığındaki verileri bulma
  @Query("{'timestamp': {$gte: ?0, $lte: ?1}}")
  List<SnmpData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

  // Son N kaydı getirme (timestamp'e göre sıralı)
  List<SnmpData> findTop10ByOrderByTimestampDesc();

  // Device IP'ye göre son kayıtları getirme
  List<SnmpData> findTop5ByDeviceIpOrderByTimestampDesc(String deviceIp);

  // Metric type'a göre son kayıtları getirme
  List<SnmpData> findTop5ByMetricTypeOrderByTimestampDesc(String metricType);

  // Device ve metric kombinasyonuna göre son kayıt
  SnmpData findFirstByDeviceIpAndMetricTypeOrderByTimestampDesc(String deviceIp, String metricType);

  // Belirli tarihten sonraki kayıtlar
  List<SnmpData> findByTimestampAfter(LocalDateTime timestamp);

  // Metric type'ına göre kayıt sayısı
  long countByMetricType(String metricType);

  // Device'a göre kayıt sayısı
  long countByDeviceIp(String deviceIp);
}
