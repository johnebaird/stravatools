package com.example.demo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)

public class Bikes implements Serializable {

    private String id;
    private boolean primary;
    private long distance;
    private String name;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public boolean isPrimary() {
        return primary;
    }
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
    public long getDistance() {
        return distance;
    }
    public void setDistance(long distance) {
        this.distance = distance;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

        
    

    
}
