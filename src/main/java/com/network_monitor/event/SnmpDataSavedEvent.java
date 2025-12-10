package com.network_monitor.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.network_monitor.model.SnmpData;

import lombok.ToString;

@ToString
public class SnmpDataSavedEvent extends ApplicationEvent {

    private final List<SnmpData> snmpData;
    private final String frequencyType;
    private final String userId;

    public SnmpDataSavedEvent(Object source, List<SnmpData> snmpData, String frequencyType, String userId) {
        super(source);
        this.snmpData = snmpData;
        this.frequencyType = frequencyType;
        this.userId = userId;
    }

    public List<SnmpData> getSnmpData() {
        return snmpData;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public String getUserId() {
        return userId;
    }
}