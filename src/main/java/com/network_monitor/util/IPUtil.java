package com.network_monitor.util;

import jakarta.servlet.http.HttpServletRequest;

public class IPUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "X-Real-IP"
    };

    public static String getClientIP(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Birden fazla IP varsa ilkini al (gerçek client IP)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return normalizeIP(ip);
            }
        }

        return normalizeIP(request.getRemoteAddr());
    }

    private static String normalizeIP(String ip) {
        if (ip == null) {
            return "unknown";
        }

        // IPv6 localhost -> IPv4 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }
}