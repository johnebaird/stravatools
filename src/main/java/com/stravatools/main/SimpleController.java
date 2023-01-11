package com.stravatools.main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.stravatools.main.model.Activity;
import com.stravatools.main.model.Bearer;
import com.stravatools.main.model.Bikes;
import com.stravatools.main.model.Maintenance;
import com.stravatools.main.model.Muting;
import com.stravatools.main.model.User;
import com.stravatools.main.model.Muting.ActivityType;
import com.stravatools.main.model.repositories.BearerRepository;
import com.stravatools.main.model.repositories.MaintenanceRepository;
import com.stravatools.main.model.repositories.MutingRepository;
import com.stravatools.main.model.repositories.UserRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

@Controller
@SessionAttributes({"user", "strava"})
public class SimpleController {

    @Autowired
	private BearerRepository bearerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private MutingRepository mutingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    // user and strava interface is saved for session so we only need to look it up once
    @ModelAttribute("user")
    public User user() {
        return new User();
    }

    @ModelAttribute("strava")
    public StravaRestAPI strava() {
        return new StravaRestAPI();
    }

    @GetMapping("/")
    public String homePage(Model model) {
        return "index";
    }

    @GetMapping("/login")
    public String loginUser(Model model) {
        return "login";
    }

    @GetMapping("/stravaauth")
    public String authToStrava(Model model) {
        return "stravaauth";
    }

    @PostMapping("/register")
    public String createUser(@RequestParam(name="username", required=true) String username, @RequestParam(name="password", required=true) String password, Model model) {
        
        // create a new user
        System.out.println("running createregisteruser");

        if (userRepository.existsById(username)) {
            System.out.println("request to create existing user: " + username);
            return "redirect:/login?error=userExists";
        }
        
        else {
            System.out.println("creating new user with username: " + username + " and pass: " + password);
            User u = new User(username, passwordEncoder.encode(password));
            userRepository.save(u);

            return "redirect:/login?error=everythingIsfine";
        }
        
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(Authentication authentication, @RequestParam(name="code", required=true) String code, Model model, 
                                    @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
        
        // called by strava to save our token for Strava oauth
        strava.setClientId(clientId);
        strava.setClientSecret(clientSecret);
        
        strava.postForBearerToken(code);
                
        Bearer token = strava.getBearerToken();
        User currentUser = userRepository.findById(authentication.getName()).get();

        token.setUid(UUID.randomUUID());
        currentUser.setBearerUUID(token.getUid());

        userRepository.save(currentUser);
        bearerRepository.save(token);

        model.addAttribute("user", user);
        model.addAttribute("strava", strava);

        return "redirect:/me/activities";
    }
    
    
    // ---------------------------------------------------------------------------------------------------------------------- //
    // *** Maintenance Reminder page ------------------ //
    // set maintenanc reminder to get emailed about certain tasks to do very X kilometers on the bike
    // ---------------------------------------------------------------------------------------------------------------------- // 

    @GetMapping("/me/maintenance")
    public String maintenance(Authentication authentication, Model model, @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {

        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        Bikes[] bikes = user.getAthlete().getBikes();

        List<Maintenance> maintenance = maintenanceRepository.findByUsername(user.getUsername());
        
        model.addAttribute("bikes", bikes);
        model.addAttribute("maintenance", maintenance);
        model.addAttribute("athlete", user.getAthlete());
        return "me/maintenance";

    }

    @PostMapping("/setMaintenance")
    public String setMaintenance(Authentication authentication, @RequestParam(name="uuid", required=true) String uuid,
                                                                @RequestParam(name="bike", required=true) String bike,
                                                                @RequestParam(name="triggerEvery", required=true) String triggerEvery,
                                                                @RequestParam(name="emailAddress", required=true) String emailAddress,
                                                                @RequestParam(name="message", required=true) String message, 
                                                                @RequestParam(name="enabled", required=false) String enabled, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        UUID u;
        long trigger;
                
        if (triggerEvery.equals("")) { trigger = 0; }
        else {trigger = Long.parseLong(triggerEvery);}

        if (uuid.equals("new")) { 
            u = UUID.randomUUID(); 
            Maintenance m = new Maintenance(user.getUsername(), u, bike, emailAddress, trigger * 1000, message);

            // set current distance to 'last triggered' so we count up from now
            Bikes[] allbikes = user.getAthlete().getBikes();
            for (Bikes b : allbikes) {
                if (bike.equals(b.getId())) {
                    m.setLastTriggeredDistance(b.getDistance());
                }
            }
            
            if (enabled != null) {
                if (enabled.equals("on")) { m.setEnabled(true); }
            }
            maintenanceRepository.save(m);
        }
        else { 
            // existing maintenance activity
            u = UUID.fromString(uuid); 
            List<Maintenance> allmaintenance = maintenanceRepository.findByUsername(user.getUsername());

            for (Maintenance m : allmaintenance) {
                if (m.getUuid().equals(u)) {
                    
                    m.setBike(bike);
                    m.setTriggerEvery(trigger * 1000);
                    m.setMessage(message);

                    if (enabled != null) {
                        if (enabled.equals("on")) { m.setEnabled(true); }
                    }
                    else {
                        m.setEnabled(false);
                    }
                    maintenanceRepository.save(m);
                }
            }

        }
            
                        
        return "redirect:/me/maintenance";                 
    }

    @PostMapping("/removeMaintenance")
    public String removeMaintenance(Authentication authentication, @RequestParam(name="uuid", required=true) String uuid, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
    

        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        List<Maintenance> maintenance = maintenanceRepository.findByUsername(user.getUsername());

        for (Maintenance m : maintenance) {
            if (uuid.equals(m.getUuid().toString())) {
                logger.info("deleted maintenance activity with uuid: " + uuid);
                maintenanceRepository.delete(m);
            }
        }

    return "redirect:/me/maintenance";
    }



    // ---------------------------------------------------------------------------------------------------------------------- //
    // *** Muting feed page ------------------ //
    // methods for muting certain activities from the global feed
    // ---------------------------------------------------------------------------------------------------------------------- //



    @GetMapping("/me/muting")
    public String muting(Authentication authentication, Model model, @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {

        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        List<Muting> muting = mutingRepository.findByUsername(user.getUsername());
        
        model.addAttribute("activities", Muting.ActivityType.values());
        model.addAttribute("muting", muting);
        model.addAttribute("athlete", user.getAthlete());

        return "me/muting";

    }

    @PostMapping("/setMuting")
    public String setMaintenance(Authentication authentication, @RequestParam(name="uuid", required=true) String uuid,
                                                                @RequestParam(name="activity", required=true) ActivityType activity,
                                                                @RequestParam(name="duration", required=false) String duration,
                                                                @RequestParam(name="anyDuration", required=false) Boolean anyDuration,
                                                                @RequestParam(name="enabled", required=false) Boolean enabled, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        UUID u;
        long longDuration;
                
        if (duration == null || duration.equals("")) { longDuration = 0; }
        else {longDuration = Long.parseLong(duration);}

        if (uuid.equals("new")) { 
            u = UUID.randomUUID(); 
            Muting m = new Muting(user.getUsername(), u, activity, longDuration);

            if (anyDuration != null) {m.setAnyDuration(anyDuration);}
            else {m.setAnyDuration(false);}

            if (enabled != null) {m.setEnabled(enabled);}
            else {m.setEnabled(false);}
                        
            mutingRepository.save(m);
        }
        else { 
            // existing muting activity
            u = UUID.fromString(uuid); 
            List<Muting> allmuting = mutingRepository.findByUsername(user.getUsername());

            for (Muting m : allmuting) {
                if (m.getUuid().equals(u)) {
                    
                    m.setActivity(activity);                    
                    m.setDuration(longDuration);

                    if (anyDuration != null) {m.setAnyDuration(anyDuration);}
                    else {m.setAnyDuration(false);}
        
                    if (enabled != null) {m.setEnabled(enabled);}
                    else {m.setEnabled(false);}

                    mutingRepository.save(m);
                }
            }

        }
            
                        
        return "redirect:/me/muting";                 
    }

    @PostMapping("/removeMuting")
    public String removeMuting(Authentication authentication, @RequestParam(name="uuid", required=true) String uuid, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
    

        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        List<Muting> muting = mutingRepository.findByUsername(user.getUsername());

        for (Muting m : muting) {
            if (uuid.equals(m.getUuid().toString())) {
                logger.info("deleted muting activity with uuid: " + uuid);
                mutingRepository.delete(m);
            }
        }

    return "redirect:/me/muting";
    }


    // ---------------------------------------------------------------------------------------------------------------------- //
    // *** Default bikes page ------------------ //
    // methods for setting default bike for indoor activities and default bike for outdoor activities
    // and to change activities in a timerange to this outdoor or indoor default bike
    // ---------------------------------------------------------------------------------------------------------------------- //


    @GetMapping("/me/defaultbikes")
    public String defaultbikes(Authentication authentication, Model model, @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
        
        // show page to let user pick options relating to setting default bikes and changing activities
        if (user == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }
        
        logger.debug("Using access_token: " + strava.getBearerToken().getAccess_token());

        Bikes[] bikes = user.getAthlete().getBikes();

        String indoorBikeName = "None";
        String outdoorBikeName = "None";

        for (Bikes b : bikes) {
            if ( b.getId().equals(user.getIndoorBike()) ) {
                indoorBikeName = b.getName();
            }
            if ( b.getId().equals(user.getOutdoorBike()) ) {
                outdoorBikeName = b.getName();
            }
        }

        model.addAttribute("athlete", user.getAthlete());
        model.addAttribute("bikes", bikes);
        model.addAttribute("indoorBikeName", indoorBikeName);
        model.addAttribute("outdoorBikeName", outdoorBikeName);
        model.addAttribute("indoorBikeId", user.getIndoorBike());
        model.addAttribute("outdoorBikeId", user.getOutdoorBike());
        model.addAttribute("autoChangeIndoor", user.isAutoChangeIndoorBike());
        model.addAttribute("autoChangeOutdoor", user.isAutoChangeOutdoorBike());
                        
        return "me/defaultbikes";
    }

    @PostMapping("/setBikes")
    public String setDefaultBikes(Authentication authentication, @RequestParam(name="indoor", required=false) String indoor, 
                                                                @RequestParam(name="outdoor", required=false) String outdoor, 
                                                                @RequestParam(name="indoorAutoSwitch", required=false) String indoorAutoSwitch,
                                                                @RequestParam(name="outdoorAutoSwitch", required=false) String outdoorAutoSwitch, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {

        // set default indoor or outdour bike for user
        logger.debug(String.format("/setBikes called with parameters: indoor: %s, outdoor: %s, indoorAutoSwitch: %s, outdoorAutoSwitch: %s", 
                                    indoor, outdoor, indoorAutoSwitch, outdoorAutoSwitch));
                
        if (indoor != null) {user.setIndoorBike(indoor);}
        if (outdoor != null) {user.setOutdoorBike(outdoor);}

        if (indoorAutoSwitch != null) {user.setAutoChangeIndoorBike(true);}
        else {user.setAutoChangeIndoorBike(false);}

        if (outdoorAutoSwitch != null) {user.setAutoChangeOutdoorBike(true);}
        else {user.setAutoChangeOutdoorBike(false);}

        userRepository.save(user);
        
        return "redirect:/me/defaultbikes";
    }

    @PostMapping("/changeAllIndoor")
    public String changeAllIndoor(Authentication authentication, @RequestParam(name="start", required=true) String start,
                                                                @RequestParam(name="end", required=true) String end, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {

        // run task to change all indoor bikes for date range                                                            
        long epocStart = LocalDate.parse(start).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long epocEnd = LocalDate.parse(end).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        int changed = strava.changeBikeForActivities(epocEnd, epocStart, user.getIndoorBike(), "indoor");

        model.addAttribute("changedActivities", changed);
        
        return "redirect:/me/defaultbikes";                                                           
    }

    @PostMapping("/changeAllOutdoor")
    public String changeAllOutdoor(Authentication authentication, @RequestParam(name="start", required=true) String start,
                                                                @RequestParam(name="end", required=true) String end, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") StravaRestAPI strava) {
        // run task to change all outdoor bikes for date range                                                            
        if (user == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }

        long epocStart = LocalDate.parse(start).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long epocEnd = LocalDate.parse(end).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        int changed = strava.changeBikeForActivities(epocEnd, epocStart, user.getOutdoorBike(), "outdoor");

        model.addAttribute("changedActivities", changed);
        
        return "redirect:/me/defaultbikes";                                                           
    }


    // ---------------------------------------------------------------------------------------------------------------------- //
    // *** Activitiy View Page ------------------ //
    // generic method to display what activities we get from the API
    // ---------------------------------------------------------------------------------------------------------------------- //



    @GetMapping("/me/activities")
    public String activities(Authentication authentication, @RequestParam(name="page", required=false) Optional<Integer> page, Model model, 
                                                            @ModelAttribute("user") User user, 
                                                            @ModelAttribute("strava") StravaRestAPI strava) {
        
        // returns page that shows pages of activities
        logger.debug("activities called with user: " + user.getUsername() + " " + user.getBearerUUID());
        logger.debug("activities called with strava: " + strava.toString());

        if (user.getUsername() == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }
        
        logger.debug("activities after load user: " + user.getUsername() + " " + user.getBearerUUID());
        logger.debug("activities after load strava: " + strava.toString());


        int currentPage = 1;
        if (page.isPresent()) { currentPage = page.get(); }
        
        logger.debug("Using access_token: " + strava.getBearerToken().getAccess_token());
        
        Activity[] activities = strava.getAthleteActivities(currentPage);
        
        model.addAttribute("athlete", user.getAthlete());
        model.addAttribute("activities", activities);
        model.addAttribute("page", currentPage);
                        
        return "me/activities";
    }

    private User loadUser(Authentication authentication, Model model, User user) {
        // takes details from auth object and load user data from db as an attribute

        user = userRepository.findById(authentication.getName()).get();

        logger.debug("loadUser found user: " + user.getUsername());
        
        model.addAttribute("user", user);
        
        return user;

    }
   


    private StravaRestAPI loadBearer(User user, StravaRestAPI strava, Model model) {
        // called after load user to load user's bearer token into an attribute        

        strava.setClientId(clientId);
        strava.setClientSecret(clientSecret);

        Bearer bearer = bearerRepository.findById(user.getBearerUUID()).get();

        logger.debug("LoadBearer found bearer token: " + bearer.getRefresh_token() + " for user: " + user.getUsername());
        
        strava.setBearerToken(bearer);
        strava.refreshBearerToken();
        bearerRepository.save(strava.getBearerToken());

        user.setAthlete(strava.postforAthlete());

        model.addAttribute("strava", strava);

        return strava;

    }

}

