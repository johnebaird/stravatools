package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

@Controller
public class SimpleController {

    @Autowired
	private BearerRepository bearerRepository;


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
        try {
            Bearer b = bearerRepository.findById(UUID.fromString("2af29c16-dc7c-435b-b78a-09ec380541d0")).get();
            this.strava.setBearerToken(b);
        }
        catch (NoSuchElementException e) {
            // this is fine
        }
    }

    @GetMapping("/")
    public String homePage(Model model) {

        model.addAttribute("appname", appName);
        model.addAttribute("clientSecret", clientSecret);
        model.addAttribute("clientId", clientId);

        if (this.strava.getBearerToken() == null) {
            return "index";
        }
        else {
            return "redirect:/me";
        }
        
    }

    @PostMapping("/user_login")
    public String processLogin(@RequestParam(name="username", required=false) String username,
                                @RequestParam(name="password", required=false) String password, Model model) {

            return "redirect:/me";

    }
    
    @GetMapping("/loginUser")
    public String loginUser(Model model) {
        return "loginUser";
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(@RequestParam(name="code", required=false) String code, Model model) {
        strava.postForBearerToken(code);
        
        // single user now set default UUID
        Bearer token = strava.getBearerToken();
        token.setUid(UUID.fromString("2af29c16-dc7c-435b-b78a-09ec380541d0"));

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
