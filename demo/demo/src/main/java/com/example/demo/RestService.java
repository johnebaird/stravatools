package com.example.demo;

import org.springframework.boot.json.JsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

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

    public String getBearerToken(String exchange) { 
        String url = "https://www.strava.com/oauth/token";

        url += "?client_id=" + this.clientId;
        url += "&client_secret=" + this.clientSecret;
        url += "&code=" + exchange;
        url += "&grant_type=authorization_code";

        System.out.println("url: " + url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, request, String.class );

        return response.toString();

    }

}
