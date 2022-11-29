package com.example.demo;

import java.io.Serializable;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown=true)

@Table
public class Bearer implements Serializable {

    @PrimaryKey 
    @CassandraType(type = Name.UUID)
    private UUID uid = UUID.randomUUID();
    
    private String token_type;
    private long expires_at;
    private long expires_in;
    private String refresh_token;
    private String access_token;

    public Bearer() {}
    
    public Bearer(UUID uid) {
        this.uid = uid;
    }

    public void setToken_type(String type) {
        this.token_type = type;
    }

    public String getToken_type() {
        return this.token_type;
    }

    public void setExpires_at(long expiry) {
        this.expires_at = expiry;
    }

    public long getExpires_at() {
        return this.expires_at;
    }

    public void setExpires_in(long expires) {
        this.expires_in = expires;
    }

    public long getExpires_in() {
        return this.expires_in;
    }

    public void setRefresh_token(String token) {
        this.refresh_token = token;
    }

    public String getRefresh_token() {
        return this.refresh_token;
    }

    public void setAccess_token(String token) {
        this.access_token = token;
    }

    public String getAccess_token() {
        return this.access_token;
    }
   
    @Override
    public String toString() {
        return "{" +
                "expires_at=" + this.expires_at + 
                ", refresh_token='" + this.refresh_token + '\'' +
                ", access_token='" + this.access_token + '\'' +
                "}";
    }
}
