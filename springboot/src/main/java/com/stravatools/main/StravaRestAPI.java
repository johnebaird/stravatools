package com.stravatools.main;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;

import com.stravatools.main.model.Activity;
import com.stravatools.main.model.Athlete;
import com.stravatools.main.model.Bearer;



public class StravaRestAPI {
// Main class for interacting with Strava API

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

    public StravaRestAPI() {
        this.restTemplate = new RestTemplate();
    }

    private URIBuilder getStravaURI(String path) {
        // Generic function for building strava URI, returns builder as will typically need to 
        // continue to add to path or add uri variables
        URIBuilder builder = new URIBuilder()
                                .setScheme("https")
                                .setHost("www.strava.com")
                                .setPath("/api/v3" + path);
        return builder;
    }

    public Athlete postforAthlete() {
        // Post to API and return a copy of Athlete object

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
        // generic function to send PUT to Strava API and log success/failure
        // returns String as typically there won't be body of data on PUT
        
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
        // takes paticular activity and bike and changes activity to be using that bike
        // used by changeBikeForActivities for changing both indoor and outdoor default bike

        URIBuilder builder = getStravaURI("/activities/");
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

    public String setActivityToMuted(long activity) {
        // takes paticular activity and set hide_from_home = true
        // which will hide it from the main feed

        URIBuilder builder = getStravaURI("/activities/");
        builder.appendPath(Long.toString(activity));

        String body = String.format("{\"hide_from_home\": true}");
        
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
        // change all trainer=true rides to indoor bike or all trainer=false rides to outdoor
        // for certain time range
        // 'type' is either 'indoor' or 'outdoor' and tells us which we're changing
        
        Activity[] activities;
        int page = 1;
        int changedActivities = 0;

        while(true) {
            // don't know how many pages of (30) activities there will be so keep looping
            
            logger.info("Bike change querying page: " + page);

            activities = this.getAthleteActivities(before, after, page);
            
            // adding a bit of a delay so loop we don't query the API too quick
            try { Thread.sleep(100L); } catch (InterruptedException e) {e.printStackTrace();}

            if (type == "indoor") {
                changedActivities += changeIndoorCurrentPage(bike, activities);
            }
            else if (type == "outdoor") {
                changedActivities += changeOutdoorCurrentPage(bike, activities);
            }

            page += 1;

            logger.info("page " + page);

            // break if we don't get 30 new activities (should be last page)
            if (activities.length < 25) { break; }
            
            // break if we hit 50 pages so we don't run run forever if we never hit first condition for some reason
            if (page > 50) {break; }

        }

        return changedActivities;
    }

    public int changeIndoorCurrentPage(String bike, Activity[] activities) {
        // function run by background task, as 30 activities will probably represent a week of use for even heavy users
        // we don't need to interate through pages

        int changedActivities = 0;
        for(Activity a: activities) {

            if ((a.isTrainer() && a.getSport_type().equals("Ride")) || a.getSport_type().equals("VirtualRide")) {
                
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
        // same as above for outdoor

        int changedActivities = 0;
        for(Activity a: activities) {

            if (!a.isTrainer() && a.getSport_type().equals("Ride")) {
                
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
        // get Activities when specifying a date range and a specific 'page' of 30 activities

        URIBuilder builder = getStravaURI("/activities/");
        
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
        // get activities when only specifying a page

        URIBuilder builder = getStravaURI("/activities/");
        
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
        // get activities with no parameters, will return first 30

        URIBuilder builder = getStravaURI("/activities/");
        
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
        // helper function for above 3 activity queries to do the actual call
            
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
        // bearer tokens expire after 6h total before needing refresh and can only be refreshed if they will expire 
        // in the next 60min, should only need to check this one per client login

        if ((Instant.now().getEpochSecond() + 3500) < bearerToken.getExpires_at()) {
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
            // want to keep UUID as this is refreshing an existing token
            UUID olduuid = this.getBearerToken().getUid();
            postForBearer(uri);
            this.getBearerToken().setUid(olduuid);
        }
        catch (URISyntaxException u) {
            u.printStackTrace();
        }

    }

    public void postForBearerToken(String exchange) { 

        // initial post for Bearer token when we first authorize Strava API to the app

        URIBuilder builder = this.getStravaURI("/oauth/token");
        
        builder.addParameter("client_id", this.clientId);
        builder.addParameter("client_secret", this.clientSecret);
        builder.addParameter("code", exchange);
        builder.addParameter("grant_type", "authorization_code");


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
