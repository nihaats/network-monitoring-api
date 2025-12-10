package com.network_monitor.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.network_monitor.event.SnmpDataSavedEvent;
import com.network_monitor.model.SnmpData;
import com.network_monitor.security.jwt.JwtUtils;
import com.network_monitor.service.OidDiscoveryService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class SnmpController {

  private static final Logger logger = LoggerFactory.getLogger(SnmpController.class);

  @Autowired
  private OidDiscoveryService oidDiscoveryService;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private JwtUtils jwtUtils;

  @GetMapping("/discover-oids")
  public Map<String, String> getOids(String routerIP) {
    var oids = oidDiscoveryService.discoverAllOids("192.168.1.1");
    return oids;
  }

  @PostMapping("/receive/{frequencyType}/{id}")
  public ResponseEntity<String> receiveData(
      @PathVariable("frequencyType") String frequencyType,
      @PathVariable("id") String id,
      @RequestBody List<SnmpData> dataList) {
    eventPublisher.publishEvent(new SnmpDataSavedEvent(this, dataList, frequencyType, id));

    return ResponseEntity.ok("Data received successfully");
  }

  @GetMapping("/all-metrics")
  public List<SnmpData> getAllMetrics(HttpServletRequest request, String routerIP) {
    return null;
  }

  @GetMapping("/download-zip")
  public ResponseEntity<byte[]> downloadZip(HttpServletRequest request) throws IOException {
    String token = jwtUtils.getJwtFromCookies(request);
    Claims claims = jwtUtils.getClaimsFromToken(token);
    String userId = claims.getId();

    // ------------------------------------------------------------------------
    // 1) Agent.java dosyasının kopyasını oluştur
    // ------------------------------------------------------------------------
    String targetDir = "snmp-agent/src/config/";
    // String targetDir = "C:/src/snmp-agent/SnmpAgent/src/config/";
    Path originalFile = Paths.get(targetDir + "UserParams.java");

    String tempName = "Agent_" + userId + ".java";
    Path tempFile = Paths.get(targetDir + tempName);

    Files.copy(originalFile, tempFile, StandardCopyOption.REPLACE_EXISTING);

    // ------------------------------------------------------------------------
    // 2) Agent.java dosyasını güncelle
    // ------------------------------------------------------------------------
    String newLine = "\"" + userId + "\"";
    String content = Files.readString(tempFile);
    content = content.replaceAll("\"\"", newLine);
    Files.writeString(tempFile, content);

    // ------------------------------------------------------------------------
    // 3) Klasörü ZIP’e dönüştür (dinamik)
    // ------------------------------------------------------------------------
    Path folderPath = Paths.get("snmp-agent");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    Files.walk(folderPath)
        .filter(path -> !Files.isDirectory(path)) // sadece dosyalar
        .filter(path -> !path.equals(tempFile)) // geçici dosyayı dahil etme
        .forEach(path -> {
          try {
            Path fileToZip = path;

            // Eğer Agent.java ise yerine kopyasını ZIP'e ekle
            if (path.equals(originalFile)) {
              fileToZip = tempFile;
            }

            // ZIP giriş adı yine orijinal isim olsun
            String zipEntryName = folderPath.relativize(path).toString();

            zos.putNextEntry(new ZipEntry(zipEntryName));
            Files.copy(fileToZip, zos);
            zos.closeEntry();

          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    zos.close(); // ZIP finalize edildi
    byte[] zipBytes = baos.toByteArray();

    // ------------------------------------------------------------------------
    // 4) ZIP'i kullanıcıya indirt
    // ------------------------------------------------------------------------
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "SnmpAgent.zip");

    Files.deleteIfExists(tempFile);

    return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
  }
}