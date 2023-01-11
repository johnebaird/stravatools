package com.stravatools.main.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown=true)

public class Athlete implements Serializable {
// Class matching data returned from Strava API for Athelete
// more data is returned than what is here but can be included later if needed

    private String id;
    private String username;
    private String firstname;
    private String lastname;
    private int ftp;
    private Bikes[] bikes;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public int getFtp() {
        return ftp;
    }
    public void setFtp(int ftp) {
        this.ftp = ftp;
    }
    public Bikes[] getBikes() {
        return bikes;
    }
    public void setBikes(Bikes[] bikes) {
        this.bikes = bikes;
    }

    
}
