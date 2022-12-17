package com.stravatools.main;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.core5.net.URIBuilder;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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

    private URIBuilder getStravaURI(String path) {
        URIBuilder builder = new URIBuilder()
                                .setScheme("https")
                                .setHost("www.strava.com")
                                .setPath("/api/v3/" + path);
        return builder;
    }

    public Athlete postforAthlete() {

        String url = "https://www.strava.com/api/v3/athlete/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken.getAccess_token());

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Athlete> response = this.restTemplate.exchange(url, HttpMethod.GET, request, Athlete.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info("Successfully got athelete info");
            return response.getBody();
        }
        else {
            logger.info("Error getting Athlete" + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }

    }

    private String putToStravaAPI(URI url, String body) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken.getAccess_token());

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info("Successful PUT");
            return response.getBody();
        }
        else {
            logger.info("Error on PUT request" + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }
    }

    public String changeBikeForActivity(long activity, String bike) {

        URIBuilder builder = getStravaURI("activities/");
        builder.appendPath(Long.toString(activity));

        String body = String.format("{\"gear_id\": \"%s\"}", bike);
        
        try {
            URI uri = builder.build();
            return putToStravaAPI(uri, body);
        }
        catch (URISyntaxException u) {
            u.printStackTrace();
            return null;
        }
    }



    public int changeBikeForActivities(long before, long after, String bike, String type) {
        
        Activity[] activities;
        int page = 1;
        int changedActivities = 0;

        while(true) {
            
            logger.info("Bike change querying page: " + page);

            activities = this.getAthleteActivities(before, after, page);
            try { Thread.sleep(100L); } catch (InterruptedException e) {e.printStackTrace();}

            if (type == "indoor") {
                changedActivities += changeIndoorCurrentPage(bike, activities);
            }
            else if (type == "outdoor") {
                changedActivities += changeOutdoorCurrentPage(bike, activities);
            }

            page += 1;

            logger.info("page " + page);

            if (activities.length < 25) { break; }
            if (page > 50) {break; }

        }

        return changedActivities;
    }

    public int changeIndoorCurrentPage(String bike, Activity[] activities) {
        int changedActivities = 0;
        for(Activity a: activities) {

            if (a.isTrainer() && (a.getSport_type().equals("Ride") || a.getSport_type().equals("VirtualRide"))) {
                
                logger.info("Found indoor cadidate to change: " + a.getId() + " " + a.getName());
                
                if (!a.getGear_id().equals(bike)) {
                    this.changeBikeForActivity(a.getId(), bike);
                    try { Thread.sleep(100L); } catch (InterruptedException e) {e.printStackTrace();}
                    changedActivities += 1;
                }
            }
        }
        return changedActivities;
    }

    public int changeOutdoorCurrentPage(String bike, Activity[] activities) {
        int changedActivities = 0;
        for(Activity a: activities) {

            if (!a.isTrainer() && (a.getSport_type().equals("Ride") || a.getSport_type().equals("VirtualRide"))) {
                
                logger.info("Found outdoor cadidate to change: " + a.getId() + " " + a.getName());
                
                if (!a.getGear_id().equals(bike)) {
                    this.changeBikeForActivity(a.getId(), bike);
                    try { Thread.sleep(100L); } catch (InterruptedException e) {e.printStackTrace();}
                    changedActivities += 1;
                }
            }
        }
        return changedActivities;
    }


    public Activity[] getAthleteActivities(long before, long after, int page) {

        URIBuilder builder = getStravaURI("activities/");
        
        builder.setParameter("before", Long.toString(before));
        builder.setParameter("after", Long.toString(after)); 
        builder.setParameter("page", Integer.toString(page));

        try {

            URI uri = builder.build();
            return apiQueryActivities(uri);
        }
        
        catch (URISyntaxException u) {
            u.printStackTrace();
            return null;
        }

    }



    public Activity[] getAthleteActivities(int page) {

        URIBuilder builder = getStravaURI("activities/");
        
        builder.setParameter("page", Integer.toString(page));

        try {
            URI uri = builder.build();
            return apiQueryActivities(uri);
        }
        
        catch (URISyntaxException u) {
            u.printStackTrace();
            return null;
        }
    }

    public Activity[] getAthleteActivities() {

        URIBuilder builder = getStravaURI("activities/");
        
        try {
            URI uri = builder.build();
            return apiQueryActivities(uri);
        }
        catch (URISyntaxException u) {
            u.printStackTrace();
            return null;
        }

    }

    
    private Activity[] apiQueryActivities(URI uri) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken.getAccess_token());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Activity[]> response = this.restTemplate.exchange(uri, HttpMethod.GET, entity, Activity[].class);

   
        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info("Successfully got activities");
            return response.getBody();
        }
        else {
            logger.info("Error getting Activities" + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }
    }

    public void refreshBearerToken() {

        if ((Instant.now().getEpochSecond() + 60) < bearerToken.getExpires_at()) {
            logger.info("Bearer token still valid");
            return;
        }

        URIBuilder builder = this.getStravaURI("/oauth/token");
        
        builder.addParameter("client_id", this.clientId);
        builder.addParameter("client_secret", this.clientSecret);
        builder.addParameter("grant_type", "refresh_token");
        builder.addParameter("refresh_token", (String)bearerToken.getRefresh_token());

        try {
            URI uri = builder.build();
            postForBearer(uri);
        }
        catch (URISyntaxException u) {
            u.printStackTrace();
        }

    }

    public void postForBearerToken(String exchange) { 

        URIBuilder builder = this.getStravaURI("/oauth/token");
        
        builder.addParameter("client_id", this.clientId);
        builder.addParameter("client_secret", this.clientSecret);
        builder.addParameter("grant_type", "autorization_code");
        builder.addParameter("code", exchange);

        try {
            URI uri = builder.build();
            postForBearer(uri);
        }
        catch (URISyntaxException u) {
            u.printStackTrace();
        }

        
        
    }

    private void postForBearer(URI url) {
        ResponseEntity<Bearer> response = this.restTemplate.postForEntity(url, null, Bearer.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info("Successfully got Bearer Token: " + response.getBody().getAccess_token());
            this.bearerToken = response.getBody();
        }
        else {
            logger.info("Error getting Bearer Token" + response.getStatusCode() + " " + response.getBody());
            
        }
    }

}
