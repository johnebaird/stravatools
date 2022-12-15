package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

@Controller
@SessionAttributes("athlete")
public class SimpleController {

    @Autowired
	private BearerRepository bearerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    private RestService strava = new RestService();

    @ModelAttribute("athlete")
    public Athlete athlete() {
        return new Athlete();
    }

    @PostConstruct
    public void init() {
        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);
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

    @GetMapping("/register")
    public String registerUser(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String createregisterUser(@RequestParam(name="username", required=true) String username, @RequestParam(name="password", required=true) String password, Model model) {

        if (userRepository.existsById(username)) {
            return "redirect:/register?error=userExists";
        }
        else {
            User u = new User(username, passwordEncoder.encode(password));
            userRepository.save(u);

            return "redirect:/login";
        }
        
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(Authentication authentication, @RequestParam(name="code", required=true) String code, Model model, @ModelAttribute("athlete") Athlete athlete) {
        
        strava.postForBearerToken(code);
                
        Bearer token = strava.getBearerToken();
        User currentUser = userRepository.findById(authentication.getName()).get();

        token.setUid(UUID.randomUUID());
        currentUser.setBearerToken(token.getUid());

        userRepository.save(currentUser);
        bearerRepository.save(token);

        return "redirect:/me/activities";
    }
    
    @PostMapping("/setBikes")
    public String setDefaultBikes(Authentication authentication, @RequestParam(name="indoor", required=false) String indoor, 
                                                                @RequestParam(name="outdoor", required=false) String outdoor, Model model, 
                                                                @ModelAttribute("athlete") Athlete athlete) {

        User currentUser = userRepository.findById(authentication.getName()).get();
        
        currentUser.setIndoorBike(indoor);
        currentUser.setOutdoorBike(outdoor);

        userRepository.save(currentUser);

        return "redirect:/me/defaultbikes";
    }

    @PostMapping("/changeAllIndoor")
    public String changeAllIndoor(Authentication authentication, @RequestParam(name="start", required=true) String start,
                                                                @RequestParam(name="end", required=true) String end, Model model, 
                                                                @ModelAttribute("athlete") Athlete athlete) {

        User currentUser = userRepository.findById(authentication.getName()).get();

        long epocStart = LocalDate.parse(start).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long epocEnd = LocalDate.parse(end).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        int changed = strava.changeBikeForActivities(epocEnd, epocStart, currentUser.getIndoorBike(), "indoor");

        model.addAttribute("changedActivities", changed);
        
        return "redirect:/me/defaultbikes";                                                           
    }

    @PostMapping("/changeAllOutdoor")
    public String changeAllOutdoor(Authentication authentication, @RequestParam(name="start", required=true) String start,
                                                                @RequestParam(name="end", required=true) String end, Model model, 
                                                                @ModelAttribute("athlete") Athlete athlete) {

        User currentUser = userRepository.findById(authentication.getName()).get();

        long epocStart = LocalDate.parse(start).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long epocEnd = LocalDate.parse(end).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        int changed = strava.changeBikeForActivities(epocEnd, epocStart, currentUser.getOutdoorBike(), "outdoor");

        model.addAttribute("changedActivities", changed);
        
        return "redirect:/me/defaultbikes";                                                           
    }
    
    @GetMapping("/me/maintenance")
    public String maintenance(Authentication authentication, Model model, @ModelAttribute("athlete") Athlete athlete) {
        return "me/maintenance";

    }

    @GetMapping("/me/muting")
    public String muting(Authentication authentication, Model model, @ModelAttribute("athlete") Athlete athlete) {
        return "me/muting";

    }

    @GetMapping("/me/defaultbikes")
    public String defaultbikes(Authentication authentication, Model model, @ModelAttribute("athlete") Athlete athlete) {
        
        User currentUser = userRepository.findById(authentication.getName()).get();

        if (currentUser.getBearerToken() == null) { return "redirect:/stravaauth"; }

        Bearer currentBearer = bearerRepository.findById(currentUser.getBearerToken()).get();

        strava.setBearerToken(currentBearer);
        strava.refreshBearerToken();
        bearerRepository.save(strava.getBearerToken());

        System.out.println("Using access_token: " + strava.getBearerToken().getAccess_token());

        Athlete me = strava.postforAthlete();
        Bikes[] bikes = me.getBikes();

        String defaultIndoor = "None";
        String defaultOutdoor = "None";

        for (Bikes b : bikes) {
            if ( b.getId().equals(currentUser.getIndoorBike()) ) {
                defaultIndoor = b.getName();
            }
            if ( b.getId().equals(currentUser.getOutdoorBike()) ) {
                defaultOutdoor = b.getName();
            }
        }

        model.addAttribute("athlete", me);
        model.addAttribute("bikes", bikes);
        model.addAttribute("defaultIndoor", defaultIndoor);
        model.addAttribute("defaultOutdoor", defaultOutdoor);
                        
        return "me/defaultbikes";
    }


    @GetMapping("/me/activities")
    public String activities(Authentication authentication, @RequestParam(name="page", required=false) Optional<Integer> page, Model model, 
                                                            @ModelAttribute("athlete") Athlete athlete) {
        
        int currentPage = 1;

        if (page.isPresent()) { currentPage = page.get(); }
        
        User currentUser = userRepository.findById(authentication.getName()).get();

        if (currentUser.getBearerToken() == null) { return "redirect:/stravaauth"; }

        Bearer currentBearer = bearerRepository.findById(currentUser.getBearerToken()).get();

        strava.setBearerToken(currentBearer);
        strava.refreshBearerToken();
        bearerRepository.save(strava.getBearerToken());

        System.out.println("Using access_token: " + strava.getBearerToken().getAccess_token());

        Athlete me = strava.postforAthlete();
        Activity[] activities = strava.getAthleteActivities(currentPage);

        model.addAttribute("athlete", me);
        model.addAttribute("activities", activities);
        model.addAttribute("page", currentPage);

                        
        return "me/activities";
    }
   
}
