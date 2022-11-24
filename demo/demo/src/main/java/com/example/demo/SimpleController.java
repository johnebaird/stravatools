package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;

@Controller
public class SimpleController {
    @Value("${spring.application.name}")
    private String appName;
    private String exchangeCode;
    private RestService strava = new RestService();

    @GetMapping("/")
    public String homePage(Model model) {
        
        String clientSecret = System.getenv("CLIENT_SECRET");
        String clientId = System.getenv("CLIENT_ID");

        model.addAttribute("appname", appName);
        model.addAttribute("clientSecret", clientSecret);
        model.addAttribute("clientId", clientId);
        
        return "home";
    }

    @GetMapping("/exchange_token")
    public String exchangeToken(@RequestParam(name="code", required=false) String code, Model model) {
        this.exchangeCode = code;
        return "redirect:/me";
    }

    @GetMapping("/me")
    public String me(Model model) {
        
        strava.getBearerToken(this.exchangeCode);

        Activity[] activities = strava.getAtheleteActivities();

        System.out.println("first activity: " + activities[0].getName());
                
        model.addAttribute("mycode", this.exchangeCode);
        return "me";
    }
   
}
