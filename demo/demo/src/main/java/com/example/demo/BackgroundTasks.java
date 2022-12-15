package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

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

    // run at the top of the hour every hour
    @Scheduled(cron = "0 * * * *")
    public void correctBikes() { 

        System.out.println("Running bike correction task");

        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);

        List<User> userList = userRepository.findAll();
        
        for (User user : userList) {

            // check if we need to change anything for user
            if (user.isAutoChangeIndoorBike() || user.isAutoChangeOutdoorBike()) {
                            
                if (user.getBearerToken() == null) { break; }

                Bearer currentBearer = bearerRepository.findById(user.getBearerToken()).get();
        
                strava.setBearerToken(currentBearer);
                strava.refreshBearerToken();
                bearerRepository.save(strava.getBearerToken());
            }

            if (user.isAutoChangeIndoorBike()) {
                Activity[] activities = strava.getAthleteActivities();
                strava.changeIndoorCurrentPage(user.getIndoorBike(), activities);
            }

            if (user.isAutoChangeOutdoorBike()) {
                Activity[] activities = strava.getAthleteActivities();
                strava.changeOutdoorCurrentPage(user.getOutdoorBike(), activities);
            }
        }
        
        

    }
}


