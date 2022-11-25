package com.example.demo;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.datastax.astra.sdk.AstraClient;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;


@Controller
public class SimpleController {

    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${spring.stravatools.client_id}")
    private String clientId;
    
    @Value("${spring.stravatools.client_secret}")
    private String clientSecret;

    @Autowired
    private AstraClient astraClient;

    private RestService strava = new RestService();

    @PostConstruct
    public void init() {
        this.strava.setClientId(clientId);
        this.strava.setClientSecret(clientSecret);
    }

    @GetMapping("/")
    public String homePage(Model model) {

        model.addAttribute("appname", appName);
        model.addAttribute("clientSecret", clientSecret);
        model.addAttribute("clientId", clientId);

        model.addAttribute("dborgid", astraClient.apiDevopsOrganizations().organizationId());
        
        return "home";
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(@RequestParam(name="code", required=false) String code, Model model) {
        strava.getBearerToken(code);
        return "redirect:/me";
    }

    @GetMapping("/me")
    public String me(Model model) {

        strava.refreshBearerToken();

        Activity[] activities = strava.getAtheleteActivities();

        System.out.println("first activity: " + activities[0].getName());
                
        return "me";
    }
   
}
