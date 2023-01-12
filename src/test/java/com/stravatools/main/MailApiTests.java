package com.stravatools.main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailApiTests {

    @Test
    public void sendTestEmailThroughAPI() {
        
        assertEquals(HttpStatus.ACCEPTED, MailApi.sendEmail("johnevertsbaird@gmail.com", "test case email", "this is a test"));
        
    }
    
}
