package com.network_monitor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.network_monitor.model.SnmpData;
import com.network_monitor.repository.SnmpDataRepository;

@Service
public class SnmpDataService {

  private static final Logger logger = LoggerFactory.getLogger(SnmpDataService.class);

  @Autowired
  private SnmpDataRepository snmpDataRepository;

  /**
   * SNMP verisi kaydetme
   */
  public SnmpData saveSnmpData(SnmpData snmpData) {
    try {
      logger.info("Saving SNMP data: Device={}, Metric={}, OID={}",
          snmpData.getDeviceIp(), snmpData.getMetricType(), snmpData.getOid());

      SnmpData savedData = snmpDataRepository.save(snmpData);

      logger.info("SNMP data saved successfully with ID: {}", savedData.getId());
      return savedData;

    } catch (Exception e) {
      logger.error("Error saving SNMP data: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to save SNMP data", e);
    }
  }

  /**
   * Tüm SNMP verilerini getirme
   */
  public List<SnmpData> getAllSnmpData() {
    try {
      logger.info("Fetching all SNMP data");
      return snmpDataRepository.findAll();
    } catch (Exception e) {
      logger.error("Error fetching all SNMP data: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to fetch SNMP data", e);
    }
  }

  /**
   * ID'ye göre SNMP verisi getirme
   */
  public Optional<SnmpData> getSnmpDataById(String id) {
    try {
      logger.info("Fetching SNMP data by ID: {}", id);
      return snmpDataRepository.findById(id);
    } catch (Exception e) {
      logger.error("Error fetching SNMP data by ID {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Failed to fetch SNMP data by ID", e);
    }
  }

  /**
   * Device IP'ye göre SNMP verilerini getirme
   */
  public List<SnmpData> getSnmpDataByDeviceIp(String deviceIp) {
    try {
      logger.info("Fetching SNMP data for device: {}", deviceIp);
      return snmpDataRepository.findByDeviceIp(deviceIp);
    } catch (Exception e) {
      logger.error("Error fetching SNMP data for device {}: {}", deviceIp, e.getMessage(), e);
      throw new RuntimeException("Failed to fetch SNMP data for device", e);
    }
  }

  /**
   * Metric type'a göre SNMP verilerini getirme
   */
  public List<SnmpData> getSnmpDataByMetricType(String metricType) {
    try {
      logger.info("Fetching SNMP data for metric type: {}", metricType);
      return snmpDataRepository.findByMetricType(metricType);
    } catch (Exception e) {
      logger.error("Error fetching SNMP data for metric type {}: {}", metricType, e.getMessage(), e);
      throw new RuntimeException("Failed to fetch SNMP data for metric type", e);
    }
  }

  /**
   * Son kayıtları getirme
   */
  public List<SnmpData> getLatestSnmpData() {
    try {
      logger.info("Fetching latest SNMP data");
      return snmpDataRepository.findTop10ByOrderByTimestampDesc();
    } catch (Exception e) {
      logger.error("Error fetching latest SNMP data: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to fetch latest SNMP data", e);
    }
  }

  /**
   * Belirli device için son kayıtları getirme
   */
  public List<SnmpData> getLatestSnmpDataByDevice(String deviceIp) {
    try {
      logger.info("Fetching latest SNMP data for device: {}", deviceIp);
      return snmpDataRepository.findTop5ByDeviceIpOrderByTimestampDesc(deviceIp);
    } catch (Exception e) {
      logger.error("Error fetching latest SNMP data for device {}: {}", deviceIp, e.getMessage(), e);
      throw new RuntimeException("Failed to fetch latest SNMP data for device", e);
    }
  }

  /**
   * Belirli metric type için son kayıtları getirme
   */
  public List<SnmpData> getLatestSnmpDataByMetricType(String metricType) {
    try {
      logger.info("Fetching latest SNMP data for metric type: {}", metricType);
      return snmpDataRepository.findTop5ByMetricTypeOrderByTimestampDesc(metricType);
    } catch (Exception e) {
      logger.error("Error fetching latest SNMP data for metric type {}: {}", metricType, e.getMessage(), e);
      throw new RuntimeException("Failed to fetch latest SNMP data for metric type", e);
    }
  }

  /**
   * Tarih aralığına göre SNMP verilerini getirme
   */
  public List<SnmpData> getSnmpDataByDateRange(LocalDateTime start, LocalDateTime end) {
    try {
      logger.info("Fetching SNMP data between {} and {}", start, end);
      return snmpDataRepository.findByTimestampBetween(start, end);
    } catch (Exception e) {
      logger.error("Error fetching SNMP data by date range: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to fetch SNMP data by date range", e);
    }
  }

  /**
   * İstatistikler getirme
   */
  public SnmpDataStats getSnmpDataStats() {
    try {
      logger.info("Fetching SNMP data statistics");

      long totalRecords = snmpDataRepository.count();
      long systemDescCount = snmpDataRepository.countByMetricType("system_description");
      long systemUptimeCount = snmpDataRepository.countByMetricType("system_uptime");
      long systemNameCount = snmpDataRepository.countByMetricType("system_name");
      long interfaceCount = snmpDataRepository.countByMetricType("interface_count");

      return new SnmpDataStats(totalRecords, systemDescCount, systemUptimeCount,
          systemNameCount, interfaceCount);

    } catch (Exception e) {
      logger.error("Error fetching SNMP data statistics: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to fetch SNMP data statistics", e);
    }
  }

  /**
   * İstatistik sınıfı
   */
  public static class SnmpDataStats {
    private long totalRecords;
    private long systemDescriptionCount;
    private long systemUptimeCount;
    private long systemNameCount;
    private long interfaceCount;

    public SnmpDataStats(long totalRecords, long systemDescriptionCount,
        long systemUptimeCount, long systemNameCount, long interfaceCount) {
      this.totalRecords = totalRecords;
      this.systemDescriptionCount = systemDescriptionCount;
      this.systemUptimeCount = systemUptimeCount;
      this.systemNameCount = systemNameCount;
      this.interfaceCount = interfaceCount;
    }

    // Getters
    public long getTotalRecords() {
      return totalRecords;
    }

    public long getSystemDescriptionCount() {
      return systemDescriptionCount;
    }

    public long getSystemUptimeCount() {
      return systemUptimeCount;
    }

    public long getSystemNameCount() {
      return systemNameCount;
    }

    public long getInterfaceCount() {
      return interfaceCount;
    }
  }
}
