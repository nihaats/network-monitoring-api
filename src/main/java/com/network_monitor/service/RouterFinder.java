package com.network_monitor.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

@Service
public class RouterFinder {

    public static String findRouterIP() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "route print 0.0.0.0");
            } else {
                pb = new ProcessBuilder("bash", "-c", "ip route | grep default");
            }

            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().startsWith("0.0.0.0")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        String gateway = parts[2]; // → "192.168.1.1"
                        return gateway;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "192.168.1.1";
    }

}