package com.example.demo;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

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

    private UUID bearerToken;

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

    public UUID getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(UUID bearerToken) {
        this.bearerToken = bearerToken;
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
