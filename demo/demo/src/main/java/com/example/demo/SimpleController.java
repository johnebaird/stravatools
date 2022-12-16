package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

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
    private PasswordEncoder passwordEncoder;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    @ModelAttribute("user")
    public User user() {
        return new User();
    }

    @ModelAttribute("strava")
    public RestService strava() {
        return new RestService();
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

        System.out.println("running createregisteruser");

        if (userRepository.existsById(username)) {
            System.out.println("request to create existing user: " + username);
            return "redirect:/login?error=userExists";
            
        else {
            System.out.println("creating new user with username: " + username + " and pass: " + password);
            User u = new User(username, passwordEncoder.encode(password));
            userRepository.save(u);

            return "redirect:/login?error=everythingIsfine";
        }
        
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(Authentication authentication, @RequestParam(name="code", required=true) String code, Model model, 
                                    @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {
        
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
    
    @PostMapping("/setBikes")
    public String setDefaultBikes(Authentication authentication, @RequestParam(name="indoor", required=false) String indoor, 
                                                                @RequestParam(name="outdoor", required=false) String outdoor, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {


        
        user.setIndoorBike(indoor);
        user.setOutdoorBike(outdoor);

        userRepository.save(user);

        return "redirect:/me/defaultbikes";
    }

    @PostMapping("/changeAllIndoor")
    public String changeAllIndoor(Authentication authentication, @RequestParam(name="start", required=true) String start,
                                                                @RequestParam(name="end", required=true) String end, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {


        long epocStart = LocalDate.parse(start).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long epocEnd = LocalDate.parse(end).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        int changed = strava.changeBikeForActivities(epocEnd, epocStart, user.getIndoorBike(), "indoor");

        model.addAttribute("changedActivities", changed);
        
        return "redirect:/me/defaultbikes";                                                           
    }

    @PostMapping("/changeAllOutdoor")
    public String changeAllOutdoor(Authentication authentication, @RequestParam(name="start", required=true) String start,
                                                                @RequestParam(name="end", required=true) String end, Model model, 
                                                                @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {

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
    
    @GetMapping("/me/maintenance")
    public String maintenance(Authentication authentication, Model model, @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {
        return "me/maintenance";

    }

    @GetMapping("/me/muting")
    public String muting(Authentication authentication, Model model, @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {
        return "me/muting";

    }

    @GetMapping("/me/defaultbikes")
    public String defaultbikes(Authentication authentication, Model model, @ModelAttribute("user") User user, @ModelAttribute("strava") RestService strava) {
        
        if (user == null) { 
            user = loadUser(authentication, model, user);
            if (user.getBearerUUID() == null) {
                return "redirect:/stravaauth";
            }
            else {
                strava = loadBearer(user, strava, model);
            }
        }
        
        logger.info("Using access_token: " + strava.getBearerToken().getAccess_token());

        Bikes[] bikes = user.getAthlete().getBikes();

        String defaultIndoor = "None";
        String defaultOutdoor = "None";

        for (Bikes b : bikes) {
            if ( b.getId().equals(user.getIndoorBike()) ) {
                defaultIndoor = b.getName();
            }
            if ( b.getId().equals(user.getOutdoorBike()) ) {
                defaultOutdoor = b.getName();
            }
        }

        model.addAttribute("athlete", user.getAthlete());
        model.addAttribute("bikes", bikes);
        model.addAttribute("defaultIndoor", defaultIndoor);
        model.addAttribute("defaultOutdoor", defaultOutdoor);
                        
        return "me/defaultbikes";
    }


    @GetMapping("/me/activities")
    public String activities(Authentication authentication, @RequestParam(name="page", required=false) Optional<Integer> page, Model model, 
                                                            @ModelAttribute("user") User user, 
                                                            @ModelAttribute("strava") RestService strava) {
        
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
        
        logger.info("Using access_token: " + strava.getBearerToken().getAccess_token());
        
        Activity[] activities = strava.getAthleteActivities(currentPage);
        
        model.addAttribute("athlete", user.getAthlete());
        model.addAttribute("activities", activities);
        model.addAttribute("page", currentPage);
                        
        return "me/activities";
    }

    private User loadUser(Authentication authentication, Model model, User user) {

        user = userRepository.findById(authentication.getName()).get();

        logger.debug("loadUser found user: " + user.getUsername());
        
        model.addAttribute("user", user);
        
        return user;

    }
   


    private RestService loadBearer(User user, RestService strava, Model model) {

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

