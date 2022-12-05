package com.example.demo;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

@Table
public class User implements Serializable {

    @PrimaryKey 
    @CassandraType(type = Name.UUID)
    private UUID uid = UUID.randomUUID();

    private String username;

    private String password;

    private String indoorBike;

    private String outdoorBike;

    private boolean accountExpired;
    private boolean credentialsExpired;
    private boolean accountLocked;

    private String role;

    private UUID bearerToken;

    public User(UUID uid, String username, String password) {
        this.uid = uid;
        this.username = username;
        this.password = password;

        this.accountExpired = false;
        this.credentialsExpired = false;
        this.accountLocked = false;

        this.role = "USER";
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

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
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
