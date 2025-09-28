package com.network_monitor.config;

import java.util.Map;

public class MetricsOidConstants {
    private MetricsOidConstants() {
    }

    public static final Map<String, String> HIGH_FREQUENCY_METRICS = Map.ofEntries(
            Map.entry("interface_in_octets", "1.3.6.1.2.1.2.2.1.10.1"),
            Map.entry("interface_out_octets", "1.3.6.1.2.1.2.2.1.16.1"),
            Map.entry("interface_in_errors", "1.3.6.1.2.1.2.2.1.14.1"),
            Map.entry("interface_out_errors", "1.3.6.1.2.1.2.2.1.20.1"),
            Map.entry("ip_in_receives", "1.3.6.1.2.1.4.3.0"),
            Map.entry("ip_out_requests", "1.3.6.1.2.1.4.10.0"),
            Map.entry("tcp_curr_estab", "1.3.6.1.2.1.6.9.0"),
            Map.entry("udp_in_datagrams", "1.3.6.1.2.1.7.1.0"),
            Map.entry("udp_out_datagrams", "1.3.6.1.2.1.7.4.0"),
            Map.entry("icmp_in_msgs", "1.3.6.1.2.1.5.1.0"),
            Map.entry("icmp_out_msgs", "1.3.6.1.2.1.5.14.0"),
            Map.entry("arp_table_entry", "1.3.6.1.2.1.3.1.1.3.13.1"));

    public static final Map<String, String> MEDIUM_FREQUENCY_METRICS = Map.ofEntries(
            Map.entry("system_uptime", "1.3.6.1.2.1.1.3.0"),
            Map.entry("tcp_active_opens", "1.3.6.1.2.1.6.5.0"),
            Map.entry("interface_count", "1.3.6.1.2.1.2.1.0"),
            Map.entry("ip_forwarding", "1.3.6.1.2.1.4.1.0"),
            Map.entry("ip_default_ttl", "1.3.6.1.2.1.4.2.0"));

    public static final Map<String, String> LOW_FREQUENCY_METRICS = Map.ofEntries(
            Map.entry("system_description", "1.3.6.1.2.1.1.1.0"),
            Map.entry("system_name", "1.3.6.1.2.1.1.5.0"),
            Map.entry("system_contact", "1.3.6.1.2.1.1.4.0"),
            Map.entry("system_location", "1.3.6.1.2.1.1.6.0"),
            Map.entry("system_services", "1.3.6.1.2.1.1.7.0"));
}
