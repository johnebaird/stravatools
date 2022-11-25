package com.example.demo;

import java.io.Serializable;


// @Table
public class Bearer implements Serializable {

 //   @PrimaryKey
  //  @CassandraType(type = Name.UUID)
  //  private UUID uid = UUID.randomUUID();

    private String token_type;
    private long expires_at;
    private long expires_in;
    private String refresh_token;
    private String access_token;
    private Athlete athlete;

    public void setAthelete(Athlete a) {
        this.athlete = a;
    }

    public Athlete getAthlete() {
        return this.athlete;
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
