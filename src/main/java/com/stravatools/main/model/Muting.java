package com.stravatools.main.model;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

@Table
public class Muting {

    @PrimaryKeyColumn(name = "username", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String username;

    @PrimaryKeyColumn(name = "uuid", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @CassandraType(type = Name.UUID)
    private UUID uuid;

    public static enum ActivityType {Any, AlpineSki, BackcountrySki, Canoeing, Crossfit, EBikeRide, Elliptical, Golf, Handcycle, Hike, IceSkate, InlineSkate, Kayaking, Kitesurf, NordicSki, Ride, RockClimbing, RollerSki, Rowing, Run, Sail, Skateboard, Snowboard, Snowshoe, Soccer, StairStepper, StandUpPaddling, Surfing, Swim, Velomobile, VirtualRide, VirtualRun, Walk, WeightTraining, Wheelchair, Windsurf, Workout, Yoga}

    private ActivityType activity;
    
    private long duration;

    private boolean anyDuration;

    private boolean enabled;

    public Muting(String username, UUID uuid, ActivityType activity, long duration) {
        this.username = username;
        this.uuid = uuid;
        this.activity = activity;
        this.duration = duration;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ActivityType getActivity() {
        return activity;
    }

    public void setActivity(ActivityType activity) {
        this.activity = activity;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isAnyDuration() {
        return anyDuration;
    }

    public void setAnyDuration(boolean anyDuration) {
        this.anyDuration = anyDuration;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
