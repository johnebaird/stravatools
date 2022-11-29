package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
        Bearer b = bearerRepository.findById(UUID.fromString("e209a9ea-8590-4343-9cd0-d436e9f75f0a")).get();
        strava.setBearerToken(b);
    }

    @GetMapping("/")
    public String homePage(Model model) {

        model.addAttribute("appname", appName);
        model.addAttribute("clientSecret", clientSecret);
        model.addAttribute("clientId", clientId);

        if (this.strava.getBearerToken().getAccess_token() == null) {
            return "home";
        }
        else {
            return "redirect:/me";
        }
        
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(@RequestParam(name="code", required=false) String code, Model model) {
        strava.postForBearerToken(code);
        bearerRepository.save(strava.getBearerToken());
        return "redirect:/me";
    }

    @GetMapping("/me")
    public String me(Model model) {

        strava.refreshBearerToken();
        bearerRepository.save(strava.getBearerToken());

        Activity[] activities = strava.getAtheleteActivities();

        System.out.println("first activity: " + activities[0].getName());
                
        return "me";
    }
   
}
