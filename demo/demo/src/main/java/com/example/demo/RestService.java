package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {
    
    private final RestTemplate restTemplate;
    private String clientSecret;
    private String clientId;
    private String bearerToken;

    public void setClientSecret(String secret) {
        this.clientSecret = secret;
    }

    public void setClientId(String id) {
        this.clientId = id;
    }

    public RestService() {
        this.restTemplate = new RestTemplate();
        this.clientSecret = System.getenv("CLIENT_SECRET");
        this.clientId = System.getenv("CLIENT_ID");
    }

    public Activity[] getAtheleteActivities() {
        
        String url = "https://www.strava.com/api/v3/activities/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.bearerToken);

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

    public void getBearerToken(String exchange) { 
        String url = "https://www.strava.com/oauth/token";

        url += "?client_id=" + this.clientId;
        url += "&client_secret=" + this.clientSecret;
        url += "&code=" + exchange;
        url += "&grant_type=authorization_code";

        System.out.println("url: " + url);


        ResponseEntity<Bearer> response = this.restTemplate.postForEntity(url, null, Bearer.class);


        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully got Bearer Token: " + response.getBody().getAccess_token());
            this.bearerToken = response.getBody().getAccess_token();
        }
        else {
            System.out.println("Error getting Bearer Token" + response.getStatusCode() + " " + response.getBody());
            
        }
    }

}
