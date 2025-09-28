package com.network_monitor.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.network_monitor.model.SnmpData;
import com.network_monitor.service.OidDiscoveryService;
import com.network_monitor.service.SnmpDataService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class SnmpController {

  private static final Logger logger = LoggerFactory.getLogger(SnmpController.class);

  @Autowired
  private SnmpDataService snmpDataService;

  @Autowired
  private OidDiscoveryService oidDiscoveryService;

  @GetMapping("/discover-oids")
  public Map<String, String> getOids(String routerIP) {
    var oids = oidDiscoveryService.discoverAllOids("192.168.1.1");
    return oids;
  }

  @GetMapping("/all-metrics")
  public List<SnmpData> getAllMetrics(String routerIP) {
    return snmpDataService.fetchAllMetrics();
  }

  // @PostMapping("/snmp-data/batch")
  // public void receiveSnmpDataBatch() {
  // snmpDataService.processBatch();
  // }
}