package com.stravatools.main;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.stravatools.main.model.Activity;
import com.stravatools.main.model.Bearer;
import com.stravatools.main.model.Bikes;
import com.stravatools.main.model.Maintenance;
import com.stravatools.main.model.Muting;
import com.stravatools.main.model.User;
import com.stravatools.main.model.repositories.BearerRepository;
import com.stravatools.main.model.repositories.MaintenanceRepository;
import com.stravatools.main.model.repositories.MutingRepository;
import com.stravatools.main.model.repositories.UserRepository;

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

    @Autowired
    private MutingRepository mutingRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    private StravaRestAPI strava = new StravaRestAPI();

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
    public void muteActivities() {
        logger.info("Running activity muting task");

        List<Muting> allmuting = mutingRepository.findAll();

        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);

        for (Muting m : allmuting) {

            logger.info("Checking Muting id " + m.getUuid() + " for " + m.getUsername());

            if (m.isEnabled()) {
                logger.debug("looking up user " + m.getUsername());
                User user = userRepository.findByUsername(m.getUsername());

                if (user.getBearerUUID() == null) { break; }

                logger.debug("getting bearer token for " + user.getUsername());
                Bearer currentBearer = bearerRepository.findById(user.getBearerUUID()).get();
        
                strava.setBearerToken(currentBearer);
                strava.refreshBearerToken();
                bearerRepository.save(strava.getBearerToken());

                Activity[] allActivities = strava.getAthleteActivities();


                for (Activity a : allActivities) {

                    logger.debug("Checking activity id " + a.getId() + " - " + a.getDescription() + " of type " + a.getSport_type() );

                    // if anyDuration is set change all activities that match the activity type
                    if (m.isAnyDuration()) {
                        if (a.isHide_from_home() == false && a.getSport_type().equals(m.getActivity().toString())) {
                            logger.debug("Muting activity id " + a.getId());
                            strava.setActivityToMuted(a.getId());
                        }
                    }
                    else {
                        // any duration not set need to check duration of activity is less than set limit
                        if (a.isHide_from_home() == false && a.getSport_type().equals(m.getActivity().toString()) && a.getElapsed_time() < (m.getDuration() * 60)) {
                            logger.debug("Muting activity id " + a.getId());
                            strava.setActivityToMuted(a.getId());
                        }
                    }

                }
            }
        }

    }

    @Scheduled(cron = "0 30 * * * ?")
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


