package com.stravatools.main.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown=true)
public class Activity implements Serializable {
// Class matching data returned from Strava API for activities

    private long id;
    private String name;
    private String type;
    private String gear_id;
    private String sport_type;
    private long elapsed_time;
    private boolean commute;
    private boolean trainer;
    private String description;
    private String start_date;
    
        public String getStart_date() {
        return start_date;
    }
    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public long getElapsed_time() {
        return elapsed_time;
    }
    public void setElapsed_time(long elapsed_time) {
        this.elapsed_time = elapsed_time;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getGear_id() {
        return gear_id;
    }
    public void setGear_id(String gear_id) {
        this.gear_id = gear_id;
    }
    public String getSport_type() {
        return sport_type;
    }
    public void setSport_type(String sport_type) {
        this.sport_type = sport_type;
    }
    public boolean isCommute() {
        return commute;
    }
    public void setCommute(boolean commute) {
        this.commute = commute;
    }
    public boolean isTrainer() {
        return trainer;
    }
    public void setTrainer(boolean trainer) {
        this.trainer = trainer;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    
        
}
