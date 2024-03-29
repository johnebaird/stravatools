package com.stravatools.main.model;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

@Table
public class Maintenance implements Serializable {

    @PrimaryKeyColumn(name = "username", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String username;

    @PrimaryKeyColumn(name = "uuid", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @CassandraType(type = Name.UUID)
    private UUID uuid;

    private String bike;

    private String emailAddress;

    private long lastTriggeredDistance;

    private long triggerEvery;

    private String message;

    private boolean enabled;

    public Maintenance() {
        this.uuid = UUID.randomUUID();
        this.enabled = false;
    }

    public Maintenance(String username, UUID uuid, String bike, String emailAddress, long triggerEvery, String message) {
        this.username = username;
        this.uuid = uuid;
        this.bike = bike;
        this.emailAddress = emailAddress;
        this.triggerEvery = triggerEvery;
        this.message = message;
    }

    public long getTriggerEvery() {
        return triggerEvery;
    }

    public void setTriggerEvery(long triggerEvery) {
        this.triggerEvery = triggerEvery;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uid) {
        this.uuid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBike() {
        return bike;
    }

    public void setBike(String bike) {
        this.bike = bike;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String phoneNumber) {
        this.emailAddress = phoneNumber;
    }

    public long getLastTriggeredDistance() {
        return lastTriggeredDistance;
    }

    public void setLastTriggeredDistance(long lastTriggeredDistance) {
        this.lastTriggeredDistance = lastTriggeredDistance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    
    
    
}
