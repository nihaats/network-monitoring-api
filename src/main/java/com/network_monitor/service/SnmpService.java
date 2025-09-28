package com.network_monitor.service;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

@Service
public class SnmpService {

    private static final int DEFAULT_PORT = 161;
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final int DEFAULT_RETRIES = 1;
    private static final String DEFAULT_COMMUNITY = "public";

    public String getOIDValue(String targetIP, String oidString) throws IOException {
        return getOIDValue(targetIP, DEFAULT_COMMUNITY, oidString);
    }

    public String getOIDValue(String targetIP, String community, String oidString)
            throws IOException {

        // Transport mapping oluştur
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();

        // SNMP objesi oluştur
        Snmp snmp = new Snmp(transport);
        transport.listen();

        try {
            // Target ayarları
            CommunityTarget<UdpAddress> target = new CommunityTarget<>();
            target.setCommunity(new OctetString(community));
            target.setAddress(new UdpAddress(targetIP + "/" + DEFAULT_PORT));
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(DEFAULT_TIMEOUT); // 5 saniye timeout
            target.setRetries(DEFAULT_RETRIES);

            // PDU oluştur (Protocol Data Unit)
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oidString)));
            pdu.setType(PDU.GET);

            // İsteği gönder
            ResponseEvent<UdpAddress> response = snmp.send(pdu, target);

            // Yanıtı kontrol et
            if (response != null && response.getResponse() != null) {
                PDU responsePDU = response.getResponse();

                if (responsePDU.getErrorStatus() == 0) {
                    // Başarılı yanıt
                    VariableBinding vb = responsePDU.get(0);
                    return vb.getVariable().toString();
                } else {
                    throw new RuntimeException("SNMP hatası: " +
                            responsePDU.getErrorStatusText() +
                            " (Error Index: " + responsePDU.getErrorIndex() + ")");
                }
            } else {
                throw new RuntimeException("SNMP yanıtı alınamadı (timeout veya bağlantı hatası)");
            }

        } finally {
            // Kaynakları temizle
            snmp.close();
        }
    }

    /**
     * Ham SNMP değerini okunabilir hale çevirir.
     */
    public String toReadableValue(String oidKey, String rawValue) {
        switch (oidKey) {
            case "interface_in_octets":
            case "interface_out_octets":
                // Byte cinsinden geliyorsa, MB veya GB olarak döndür
                try {
                    long bytes = Long.parseLong(rawValue);
                    if (bytes < 1024)
                        return bytes + " B";
                    long kb = bytes / 1024;
                    if (kb < 1024)
                        return kb + " KB";
                    long mb = kb / 1024;
                    if (mb < 1024)
                        return mb + " MB";
                    long gb = mb / 1024;
                    return gb + " GB";
                } catch (NumberFormatException e) {
                    return rawValue;
                }

            case "interface_in_errors":
            case "interface_out_errors":
                return rawValue + " errors";

            case "tcp_curr_estab":
                return rawValue + " TCP connections";

            case "udp_in_datagrams":
            case "udp_out_datagrams":
                return rawValue + " UDP datagrams";

            case "icmp_in_msgs":
            case "icmp_out_msgs":
                return rawValue + " ICMP messages";

            case "arp_table_entry":
                return rawValue + " ARP entries";

            case "ip_in_receives":
            case "ip_out_requests":
                return rawValue + " IP packets";

            default:
                return rawValue; // bilinmeyen OID’ler için raw değeri döndür
        }

    }
}
