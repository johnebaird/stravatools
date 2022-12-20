package com.stravatools.main;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

@Table
public class Maintenance implements Serializable {

    @PrimaryKey 
    @CassandraType(type = Name.UUID)
    private UUID uid = UUID.randomUUID();

    private String username;

    private String bike;

    private long lastTriggeredDistance;

    private String description;

    private boolean enabled;

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
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

    public long getLastTriggeredDistance() {
        return lastTriggeredDistance;
    }

    public void setLastTriggeredDistance(long lastTriggeredDistance) {
        this.lastTriggeredDistance = lastTriggeredDistance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    
}
