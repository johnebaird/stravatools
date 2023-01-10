package com.stravatools.main;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class BackgroundTasks { 
// run background tasks at top of the hour for intermittent functions like
// correcting indoor and outdoor bikes and muting activities

    @Autowired
	private BearerRepository bearerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    private RestService strava = new RestService();

    // run at the top of the hour every hour
    @Scheduled(cron = "0 0 * * * ?")
    public void correctBikes() { 

        logger.info("Running bike correction task");

        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);

        List<User> userList = userRepository.findAll();
        
        for (User user : userList) {

            logger.debug("background task running for user: " + user.getUsername());
            
            // check if we need to change anything for user
            if (user.hasAutoTasks()) {
                            
                if (user.getBearerUUID() == null) { break; }

                logger.debug("getting bearer token for " + user.getUsername());
                Bearer currentBearer = bearerRepository.findById(user.getBearerUUID()).get();
        
                strava.setBearerToken(currentBearer);
                strava.refreshBearerToken();
                bearerRepository.save(strava.getBearerToken());
            
                if (user.isAutoChangeIndoorBike()) {
                    Activity[] activities = strava.getAthleteActivities();
                    logger.debug("changing indoor bikes for " + user.getUsername());
                    strava.changeIndoorCurrentPage(user.getIndoorBike(), activities);
                }

                if (user.isAutoChangeOutdoorBike()) {
                    Activity[] activities = strava.getAthleteActivities();
                    logger.debug("changing outdoor bikes for " + user.getUsername());
                    strava.changeOutdoorCurrentPage(user.getOutdoorBike(), activities);
                }
            }
        }

    }
    @Scheduled(cron = "0 */10 * * * ?")
    public void maintenanceReminder() { 

        logger.info("Running maintenanceReminder task");

        List<Maintenance> maintenance = maintenanceRepository.findAll();

        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);

        for (Maintenance m : maintenance) {

            logger.info("Checking Maintenance id " + m.getUuid() + " for " + m.getUsername());

            if (m.isEnabled()) {

                logger.debug("looking up user " + m.getUsername());
                User user = userRepository.findByUsername(m.getUsername());

                if (user.getBearerUUID() == null) { break; }

                logger.debug("getting bearer token for " + user.getUsername());
                Bearer currentBearer = bearerRepository.findById(user.getBearerUUID()).get();
        
                strava.setBearerToken(currentBearer);
                strava.refreshBearerToken();
                bearerRepository.save(strava.getBearerToken());

                logger.info("looking up bikes for " + user.getUsername());
                Bikes[] bikes = strava.postforAthlete().getBikes();

                for (Bikes bike : bikes) {

                    if (bike.getId().equals(m.getBike())) {
                        logger.debug("checking distance on " + bike.getName());

                        if (bike.getDistance() > m.getLastTriggeredDistance() + m.getTriggerEvery()) {
                            logger.info("Sending email for " + m.getEmailAddress() + " - " + bike.getName() + " - " + m.getMessage());
                            MailApi.sendEmail(m.getEmailAddress(), "Maintenance Reminder for " + bike.getName(), m.getMessage());
                            m.setLastTriggeredDistance(bike.getDistance());
                        }
                                                
                    }
                }

                
                
                
            }

        }
    }

}


