package com.network_monitor.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
public class OidDiscoveryService {

    public Map<String, String> discoverAllOids(String routerIP) {
        Map<String, String> discoveredOids = new HashMap<>();

        try {
            walkOidTree(routerIP, "1.3.6.1", discoveredOids);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return discoveredOids;
    }

    private void walkOidTree(String ip, String startOid, Map<String, String> results) throws IOException {
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        try {
            CommunityTarget<UdpAddress> target = new CommunityTarget<>();
            target.setCommunity(new OctetString("public"));
            target.setAddress(new UdpAddress(ip + "/161"));
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(3000);

            OID currentOid = new OID(startOid);

            // SNMP Walk Loop
            while (true) {
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(currentOid));
                pdu.setType(PDU.GETNEXT);

                ResponseEvent<UdpAddress> response = snmp.send(pdu, target);

                if (response == null || response.getResponse() == null) {
                    break; // Bitir
                }

                VariableBinding vb = response.getResponse().get(0);

                // End of MIB kontrolü
                if (vb.getOid() == null || !vb.getOid().toString().startsWith(startOid)) {
                    break;
                }

                // OID ve değeri kaydet
                results.put(vb.getOid().toString(), vb.getVariable().toString());

                // Bir sonraki OID'ye geç
                currentOid = vb.getOid();
            }

        } finally {
            snmp.close();
        }
    }
}