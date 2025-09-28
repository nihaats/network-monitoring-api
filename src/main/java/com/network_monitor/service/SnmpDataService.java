package com.network_monitor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.network_monitor.config.MetricsOidConstants;
import com.network_monitor.event.SnmpDataSavedEvent;
import com.network_monitor.model.SnmpData;
import com.network_monitor.repository.SnmpDataRepository;

@Service
public class SnmpDataService {

  // private static final Logger logger =
  // LoggerFactory.getLogger(SnmpDataService.class);
  private static final String targetIP = "192.168.1.1";
  private static final Map<String, String> highMetrics = MetricsOidConstants.HIGH_FREQUENCY_METRICS;
  private static final Map<String, String> mediumMetrics = MetricsOidConstants.MEDIUM_FREQUENCY_METRICS;
  private static final Map<String, String> lowMetrics = MetricsOidConstants.LOW_FREQUENCY_METRICS;

  @Autowired
  private SnmpDataRepository snmpDataRepository;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private SnmpService snmpService;

  public void processBatch() {
    fetchHighMetrics();
    fetchMediumMetrics();
    fetchLowMetrics();
  }

  private List<SnmpData> fetchMetrics(Map<String, String> metrics, String level) {
    List<SnmpData> dataList = new ArrayList<>();
    metrics.forEach((metricName, oid) -> {
      try {
        SnmpData data = createAndSaveSnmpData(metricName, oid, level);
        dataList.add(data);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    return dataList;
  }

  @Scheduled(fixedRate = 20_000) // 20 saniye
  private void fetchHighMetrics() {
    List<SnmpData> data = fetchMetrics(highMetrics, "high");
    eventPublisher.publishEvent(new SnmpDataSavedEvent(this, data, "high"));
  }

  @Scheduled(fixedRate = 3_600_000) // 1 saat
  private void fetchMediumMetrics() {
    List<SnmpData> data = fetchMetrics(mediumMetrics, "medium");
    eventPublisher.publishEvent(new SnmpDataSavedEvent(this, data, "medium"));
  }

  @Scheduled(fixedRate = 7_200_000) // 2 saat
  private void fetchLowMetrics() {
    List<SnmpData> data = fetchMetrics(lowMetrics, "low");
    eventPublisher.publishEvent(new SnmpDataSavedEvent(this, data, "low"));
  }

  public List<SnmpData> fetchAllMetrics() {
    List<SnmpData> allData = new ArrayList<>();
    allData.addAll(fetchMetrics(highMetrics, "high"));
    allData.addAll(fetchMetrics(mediumMetrics, "medium"));
    allData.addAll(fetchMetrics(lowMetrics, "low"));

    return allData;
  }

  private SnmpData createAndSaveSnmpData(String metricName, String oid, String frequencyType) throws IOException {
    String value = snmpService.getOIDValue(targetIP, oid);

    SnmpData snmpData = new SnmpData();
    snmpData.setDeviceIp(targetIP);
    snmpData.setOid(oid);
    snmpData.setValue(value);
    snmpData.setReadableValue(snmpService.toReadableValue(metricName, value));
    snmpData.setMetricType(metricName);
    snmpData.setFrequencyType(frequencyType);
    // snmpDataRepository.save(snmpData);

    return snmpData;
  }

}
