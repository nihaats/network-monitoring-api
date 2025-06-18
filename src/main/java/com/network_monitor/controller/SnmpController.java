package com.network_monitor.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.network_monitor.model.SnmpData;
import com.network_monitor.service.SnmpDataService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class SnmpController {

  private static final Logger logger = LoggerFactory.getLogger(SnmpController.class);

  @Autowired
  private SnmpDataService snmpDataService;

  /**
   * SNMP verisi alma endpoint (Python agent için)
   */
  @PostMapping("/snmp-data")
  public ResponseEntity<?> receiveSnmpData(@RequestBody Map<String, Object> payload) {
    try {
      logger.info("Received SNMP data: {}", payload);

      // Payload validation
      if (!isValidPayload(payload)) {
        return ResponseEntity.badRequest()
            .body(Map.of(
                "status", "error",
                "message", "Invalid payload. Required fields: oid, value, device_ip, metric_type"));
      }

      // Payload'dan SnmpData objesi oluştur
      SnmpData snmpData = new SnmpData();
      snmpData.setOid((String) payload.get("oid"));
      snmpData.setValue((String) payload.get("value"));
      snmpData.setDeviceIp((String) payload.get("device_ip"));
      snmpData.setMetricType((String) payload.get("metric_type"));

      // Readable value varsa ekle
      if (payload.containsKey("readable_value")) {
        snmpData.setReadableValue((String) payload.get("readable_value"));
      }

      // Timestamp parse etme
      String timestampStr = (String) payload.get("timestamp");
      if (timestampStr != null) {
        try {
          // ISO format'tan LocalDateTime'a çevirme
          LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          snmpData.setTimestamp(timestamp);
        } catch (DateTimeParseException e) {
          logger.warn("Invalid timestamp format, using current time: {}", timestampStr);
          snmpData.setTimestamp(LocalDateTime.now());
        }
      } else {
        snmpData.setTimestamp(LocalDateTime.now());
      }

      // Veriyi kaydet
      SnmpData savedData = snmpDataService.saveSnmpData(snmpData);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "SNMP data saved successfully",
          "data", savedData));

    } catch (Exception e) {
      logger.error("Error processing SNMP data: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to process SNMP data: " + e.getMessage()));
    }
  }

  /**
   * Tüm SNMP verilerini getirme
   */
  @GetMapping("/snmp-data")
  public ResponseEntity<?> getAllSnmpData() {
    try {
      List<SnmpData> data = snmpDataService.getAllSnmpData();
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "count", data.size(),
          "data", data));
    } catch (Exception e) {
      logger.error("Error fetching all SNMP data: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch SNMP data: " + e.getMessage()));
    }
  }

  /**
   * ID'ye göre SNMP verisi getirme
   */
  @GetMapping("/snmp-data/{id}")
  public ResponseEntity<?> getSnmpDataById(@PathVariable String id) {
    try {
      Optional<SnmpData> data = snmpDataService.getSnmpDataById(id);
      if (data.isPresent()) {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "data", data.get()));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                "status", "error",
                "message", "SNMP data not found with ID: " + id));
      }
    } catch (Exception e) {
      logger.error("Error fetching SNMP data by ID: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch SNMP data: " + e.getMessage()));
    }
  }

  /**
   * Device IP'ye göre SNMP verilerini getirme
   */
  @GetMapping("/snmp-data/device/{deviceIp}")
  public ResponseEntity<?> getSnmpDataByDevice(@PathVariable String deviceIp) {
    try {
      List<SnmpData> data = snmpDataService.getSnmpDataByDeviceIp(deviceIp);
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "device_ip", deviceIp,
          "count", data.size(),
          "data", data));
    } catch (Exception e) {
      logger.error("Error fetching SNMP data by device: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch SNMP data: " + e.getMessage()));
    }
  }

  /**
   * Metric type'a göre SNMP verilerini getirme
   */
  @GetMapping("/snmp-data/metric/{metricType}")
  public ResponseEntity<?> getSnmpDataByMetric(@PathVariable String metricType) {
    try {
      List<SnmpData> data = snmpDataService.getSnmpDataByMetricType(metricType);
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "metric_type", metricType,
          "count", data.size(),
          "data", data));
    } catch (Exception e) {
      logger.error("Error fetching SNMP data by metric: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch SNMP data: " + e.getMessage()));
    }
  }

  /**
   * Son kayıtları getirme
   */
  @GetMapping("/snmp-data/latest")
  public ResponseEntity<?> getLatestSnmpData() {
    try {
      List<SnmpData> data = snmpDataService.getLatestSnmpData();
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "count", data.size(),
          "data", data));
    } catch (Exception e) {
      logger.error("Error fetching latest SNMP data: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch latest SNMP data: " + e.getMessage()));
    }
  }

  /**
   * Device'a göre son kayıtları getirme
   */
  @GetMapping("/snmp-data/latest/device/{deviceIp}")
  public ResponseEntity<?> getLatestSnmpDataByDevice(@PathVariable String deviceIp) {
    try {
      List<SnmpData> data = snmpDataService.getLatestSnmpDataByDevice(deviceIp);
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "device_ip", deviceIp,
          "count", data.size(),
          "data", data));
    } catch (Exception e) {
      logger.error("Error fetching latest SNMP data by device: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch latest SNMP data: " + e.getMessage()));
    }
  }

  /**
   * Metric type'a göre son kayıtları getirme
   */
  @GetMapping("/snmp-data/latest/metric/{metricType}")
  public ResponseEntity<?> getLatestSnmpDataByMetric(@PathVariable String metricType) {
    try {
      List<SnmpData> data = snmpDataService.getLatestSnmpDataByMetricType(metricType);
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "metric_type", metricType,
          "count", data.size(),
          "data", data));
    } catch (Exception e) {
      logger.error("Error fetching latest SNMP data by metric: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch latest SNMP data: " + e.getMessage()));
    }
  }

  /**
   * İstatistikleri getirme
   */
  @GetMapping("/snmp-data/stats")
  public ResponseEntity<?> getSnmpDataStats() {
    try {
      SnmpDataService.SnmpDataStats stats = snmpDataService.getSnmpDataStats();
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "stats", Map.of(
              "total_records", stats.getTotalRecords(),
              "system_description_count", stats.getSystemDescriptionCount(),
              "system_uptime_count", stats.getSystemUptimeCount(),
              "system_name_count", stats.getSystemNameCount(),
              "interface_count", stats.getInterfaceCount())));
    } catch (Exception e) {
      logger.error("Error fetching SNMP data stats: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "error",
              "message", "Failed to fetch SNMP data stats: " + e.getMessage()));
    }
  }

  /**
   * Sistem durumu kontrolü
   */
  @GetMapping("/health")
  public ResponseEntity<?> healthCheck() {
    return ResponseEntity.ok(Map.of(
        "status", "healthy",
        "timestamp", LocalDateTime.now(),
        "service", "Network Monitor API",
        "version", "1.0.0"));
  }

  /**
   * API bilgisi
   */
  @GetMapping("/info")
  public ResponseEntity<?> apiInfo() {
    return ResponseEntity.ok(Map.of(
        "service", "Network Monitor API",
        "version", "1.0.0",
        "description", "SNMP Network Monitoring REST API",
        "endpoints", Map.of(
            "POST /api/v1/snmp-data", "Create SNMP data",
            "GET /api/v1/snmp-data", "Get all SNMP data",
            "GET /api/v1/snmp-data/{id}", "Get SNMP data by ID",
            "GET /api/v1/snmp-data/device/{deviceIp}", "Get SNMP data by device",
            "GET /api/v1/snmp-data/metric/{metricType}", "Get SNMP data by metric",
            "GET /api/v1/snmp-data/latest", "Get latest SNMP data",
            "GET /api/v1/snmp-data/stats", "Get SNMP data statistics",
            "GET /api/v1/health", "Health check")));
  }

  /**
   * Payload validation helper
   */
  private boolean isValidPayload(Map<String, Object> payload) {
    return payload != null &&
        payload.containsKey("oid") &&
        payload.containsKey("value") &&
        payload.containsKey("device_ip") &&
        payload.containsKey("metric_type") &&
        payload.get("oid") != null &&
        payload.get("value") != null &&
        payload.get("device_ip") != null &&
        payload.get("metric_type") != null;
  }
}