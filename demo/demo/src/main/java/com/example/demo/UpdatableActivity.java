package com.example.demo;

public class UpdatableActivity {

    private boolean commute;

    private boolean trainer;

    private boolean hide_from_home;

    private String description;
    
    private String name;

    private String sport_type;

    private String gear_id;

    private UpdatableActivity(Activity a) {
        this.commute = a.isCommute();
        this.trainer = a.isTrainer();
        this.hide_from_home = a.isHide_from_home();
        this.description = a.getDescription();
        this.name = a.getName();
        this.sport_type = a.getSport_type();
        this.gear_id = a.getGear_id();
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

    public boolean isHide_from_home() {
        return hide_from_home;
    }

    public void setHide_from_home(boolean hide_from_home) {
        this.hide_from_home = hide_from_home;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSport_type() {
        return sport_type;
    }

    public void setSport_type(String sport_type) {
        this.sport_type = sport_type;
    }

    public String getGear_id() {
        return gear_id;
    }

    public void setGear_id(String gear_id) {
        this.gear_id = gear_id;
    }

    
    
}
