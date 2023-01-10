package com.stravatools.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Component
public class MailApi {
    
    private static String apikey;
    
    private static String fromAddress;

    @Value("${sendgrid_from_address}")
    public void setFromAddress(String address){
        MailApi.fromAddress = address;
    }

    @Value("${sendgrid_api_key}")
    public void setApiKey(String key){
        MailApi.apikey = key;
    }
    

    private static RestTemplate restTemplate = new RestTemplate();
    private static Logger logger = LoggerFactory.getLogger(MailApi.class);
    
    public static String sendEmail(String to, String subject, String content) {

        logger.debug("sendEmail called with apikey: " + apikey);

        String url = "https://api.sendgrid.com/v3/mail/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apikey);

        String body = String.format("{\"personalizations\": [{\"to\": [{\"email\": \"%s\"}]}],\"from\": "+
                                    "{\"email\": \"%s\"},\"subject\": \"%s\",\"content\": "+
                                    "[{\"type\": \"text/plain\", \"value\": \"%s\"}]}", to, fromAddress, subject, content);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            logger.info("Successfully sent email to " + to);
            return response.getBody();
        }
        else {
            logger.info("Error sending email to " + to + " " + response.getStatusCode() + " " + response.getBody());
            return null;
            
        }


    }

}


/*
 * 
 * 
curl --request POST \
  --url https://api.sendgrid.com/v3/mail/send \
  --header "Authorization: Bearer $SENDGRID_API_KEY" \
  --header 'Content-Type: application/json' \
  --data '{"personalizations": [{"to": [{"email": "test@example.com"}]}],"from": {"email": "test@example.com"},"subject": "Maintenance Reminder","content": [{"type": "text/plain", "value": "and easy to do anywhere, even with cURL"}]}'
 */