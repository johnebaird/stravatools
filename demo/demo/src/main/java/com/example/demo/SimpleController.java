package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

@Controller
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

    @PostConstruct
    public void init() {
        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);
/*        try {
            Bearer b = bearerRepository.findById(UUID.fromString("2af29c16-dc7c-435b-b78a-09ec380541d0")).get();
            this.strava.setBearerToken(b);
        }
        catch (NoSuchElementException e) {
            // this is fine
        }
*/
    }

    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/")
    public String homePage(Model model) {

        model.addAttribute("appname", appName);
        model.addAttribute("clientSecret", clientSecret);
        model.addAttribute("clientId", clientId);

        return "index";

        
    }

    @GetMapping("/login")
    public String loginUser(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String registerUser(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String createregisterUser(@RequestParam(name="username", required=true) String username, @RequestParam(name="password", required=true) String password, Model model) {

        User u = new User(username, passwordEncoder.encode(password));
        userRepository.save(u);

        return "redirect:/index";
        
    }

    @GetMapping("/exchange_token")

    public String exchangeToken(Authentication authentication, @RequestParam(name="code", required=false) String code, Model model) {
        strava.postForBearerToken(code);
        
        // single user now set default UUID
        Bearer token = strava.getBearerToken();

        User loggedInUser = (User) authentication.getDetails();
        
        token.setUid(UUID.randomUUID());
        loggedInUser.setBearerToken(token.getUid());

        userRepository.save(loggedInUser);
        bearerRepository.save(token);

        return "redirect:/me";
    }
    
    @GetMapping("/me")
    public String me(Model model) {
        

        strava.refreshBearerToken();
        bearerRepository.save(strava.getBearerToken());

        System.out.println("Using access_token: " + strava.getBearerToken().getAccess_token());

        Athlete me = strava.postforAthlete();
        Bikes[] bikes = me.getBikes();
        Activity[] activities = strava.getAtheleteActivities();

        model.addAttribute("athlete", me);
        model.addAttribute("bikes", bikes);
        model.addAttribute("activities", activities);
                        
        return "me";
    }
   
}
