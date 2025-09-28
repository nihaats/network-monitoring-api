package com.network_monitor.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.network_monitor.model.SnmpData;

import lombok.ToString;

@ToString
public class SnmpDataSavedEvent extends ApplicationEvent {

    private final List<SnmpData> snmpData;
    private final String frequencyType;

    public SnmpDataSavedEvent(Object source, List<SnmpData> snmpData, String frequencyType) {
        super(source);
        this.snmpData = snmpData;
        this.frequencyType = frequencyType;
    }

    public List<SnmpData> getSnmpData() {
        return snmpData;
    }

    public String getFrequencyType() {
        return frequencyType;
    }
}