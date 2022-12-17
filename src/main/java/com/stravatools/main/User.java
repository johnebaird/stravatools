package com.stravatools.main;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.annotation.Transient;

@Table
public class User implements Serializable {

    @PrimaryKey 
    private String username;

    private String password;

    private String indoorBike;
    private boolean autoChangeIndoorBike;

    private String outdoorBike;
    private boolean autoChangeOutdoorBike;

    private boolean accountExpired;
    private boolean credentialsExpired;
    private boolean accountLocked;

    private String role;

    private UUID bearerUUID;

    @Transient
    private Athlete athlete;


    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;

        this.accountExpired = false;
        this.credentialsExpired = false;
        this.accountLocked = false;

        this.autoChangeIndoorBike = false;
        this.autoChangeOutdoorBike = false;

        this.role = "USER";
    }

    public boolean hasAutoTasks() {
        return autoChangeIndoorBike || autoChangeOutdoorBike;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    public boolean isAutoChangeIndoorBike() {
        return autoChangeIndoorBike;
    }

    public void setAutoChangeIndoorBike(boolean changeIndoorBike) {
        this.autoChangeIndoorBike = changeIndoorBike;
    }

    public boolean isAutoChangeOutdoorBike() {
        return autoChangeOutdoorBike;
    }

    public void setAutoChangeOutdoorBike(boolean changeOutdoorBike) {
        this.autoChangeOutdoorBike = changeOutdoorBike;
    }

    public UUID getBearerUUID() {
        return bearerUUID;
    }

    public void setBearerUUID(UUID bearerUUID) {
        this.bearerUUID = bearerUUID;
    }

    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIndoorBike() {
        return indoorBike;
    }

    public void setIndoorBike(String indoorBike) {
        this.indoorBike = indoorBike;
    }

    public String getOutdoorBike() {
        return outdoorBike;
    }

    public void setOutdoorBike(String outdoorBike) {
        this.outdoorBike = outdoorBike;
    }

}
