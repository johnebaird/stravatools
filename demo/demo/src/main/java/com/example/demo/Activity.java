package com.example.demo;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Activity implements Serializable {

    private long id;
    private String name;
    private String type;
    private String gear_id;

    public void setId(long i) {
        this.id = i;
    }

    public long getId() {
        return this.id;
    }    

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return this.name;
    }

    public void setType(String t) {
        this.type = t;
    }

    public String getType() {
        return this.type;
    }

    public void setGear_id(String g) {
        this.gear_id = g;
    }

    public String getGear_id() {
        return this.gear_id;
    }
        
}
