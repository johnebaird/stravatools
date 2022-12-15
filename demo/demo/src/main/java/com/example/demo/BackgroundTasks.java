package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jnr.ffi.Struct.u_int16_t;
import jnr.ffi.Struct.u_int32_t;

@Configuration
@EnableScheduling
public class BackgroundTasks { 

    @Autowired
	private BearerRepository bearerRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    private RestService strava = new RestService();

    @Scheduled(fixedDelay = 5000)
    public void correctBikes() { 

        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);

        List<User> userList = userRepository.findAll();
        
        for (User u : userList) {

            // check if we need to change anything for user
            if (u.isAutoChangeIndoorBike() || u.isAutoChangeOutdoorBike()) {
                
                if (u.getBearerToken() == null) { break; }

                Bearer currentBearer = bearerRepository.findById(u.getBearerToken()).get();
        
                strava.setBearerToken(currentBearer);
                strava.refreshBearerToken();
                bearerRepository.save(strava.getBearerToken());
            }

            if (u.isAccountExpired()) {

                // Activity[] activities = strava.getAthleteActivities();


            }

            

                //change outdoor bike

            
            

        }
        
        System.out.println("Running bike correction task");

    }
}


