package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.Instant;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class RestService {

    private final RestTemplate restTemplate;
    private String clientSecret;
    private String clientId;
    private Bearer bearerToken;

    public Bearer getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(Bearer bearerToken) {
        this.bearerToken = bearerToken;
    }

    public void setClientSecret(String secret) {
        this.clientSecret = secret;
    }

    public void setClientId(String id) {
        this.clientId = id;
    }

    public RestService() {
        this.restTemplate = new RestTemplate();
    }

    public Athlete postforAthlete() {

        String url = "https://www.strava.com/api/v3/athlete/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken.getAccess_token());

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Athlete> response = this.restTemplate.exchange(url, HttpMethod.GET, request, Athlete.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully got athelete info");
            return response.getBody();
        }
        else {
            System.out.println("Error getting Athlete" + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }

    }

    public Activity[] getAtheleteActivities() {
        
        String url = "https://www.strava.com/api/v3/activities/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken.getAccess_token());

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Activity[]> response = this.restTemplate.exchange(url, HttpMethod.GET, request, Activity[].class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully got activities");
            return response.getBody();
        }
        else {
            System.out.println("Error getting Activities" + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }

    }

    public void refreshBearerToken() {

        if ((Instant.now().getEpochSecond() + 60) < bearerToken.getExpires_at()) {
            System.out.println("Bearer token still valid");
            return;
        }

        String url = "https://www.strava.com/oauth/token";

        url += "?client_id=" + this.clientId;
        url += "&client_secret=" + this.clientSecret;
        url += "&grant_type=refresh_token";
        url += "&refresh_token=" + (String)bearerToken.getRefresh_token();
        
        ResponseEntity<Bearer> response = this.restTemplate.postForEntity(url, null, Bearer.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully refreshed Bearer Token: " + response.getBody().getAccess_token());
            this.bearerToken = response.getBody();
        }
        else {
            System.out.println("Error refreshing Bearer Token" + response.getStatusCode() + " " + response.getBody());
            
        }

    }

    public void postForBearerToken(String exchange) { 

        String url = "https://www.strava.com/oauth/token";

        url += "?client_id=" + this.clientId;
        url += "&client_secret=" + this.clientSecret;
        url += "&code=" + exchange;
        url += "&grant_type=authorization_code";

        ResponseEntity<Bearer> response = this.restTemplate.postForEntity(url, null, Bearer.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully got Bearer Token: " + response.getBody().getAccess_token());
            this.bearerToken = response.getBody();
        }
        else {
            System.out.println("Error getting Bearer Token" + response.getStatusCode() + " " + response.getBody());
            
        }
    }

}
