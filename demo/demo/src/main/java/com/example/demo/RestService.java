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

    public String changeBikeForActivity(long activity, String bike) {

        String url = String.format("https://www.strava.com/api/v3/activities/%d", activity);
        String body = String.format("{\"gear_id\": \"%s\"}", bike);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken.getAccess_token());

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully changed bike for " + activity);
            return response.getBody();
        }
        else {
            System.out.println("Error changing bike for" + activity + " - error:" + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }

    }

    public void changeAllTrainerActivities(long after, String bike) {
        
        Activity[] activities;
        int page = 1;

        while(true) {
            
            System.out.println("Bike change querying page: " + page);

            activities = this.getAtheleteActivities(after, page);

            for(Activity a: activities) {

                if (a.isTrainer() && (a.getSport_type().equals("Ride") || a.getSport_type().equals("VirtualRide"))) {
                    
                    System.out.println("Found cadidate to change: " + a.getId() + " " + a.getName());
                    
                    if (!a.getGear_id().equals(bike)) {
                        this.changeBikeForActivity(a.getId(), bike);
                    }
                }
            }

            try { Thread.sleep(1000L); } catch (InterruptedException e) {e.printStackTrace();}

            page += 1;

            if (activities.length < 25) { break; }
            if (page > 50) {break; }

        }

    }


    public Activity[] getAtheleteActivities(long after, int page) {

        String url;

        if (after == 0) {
                url = String.format("https://www.strava.com/api/v3/activities/?page=%d", page); 
        } else {
                url = String.format("https://www.strava.com/api/v3/activities/?after=%d&page=%d", after, page); 
        }

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
