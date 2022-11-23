package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.ui.Model;

@Controller
public class SimpleController {
    @Value("${spring.application.name}")
    private String appName;
    private Athlete currentUser = new Athlete();
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
        currentUser.setCode(code);
        return "redirect:/me";
    }

    @GetMapping("/me")
    public String me(Model model) {
        String mycode = currentUser.getCode();
        String activities = strava.getBearerToken(mycode);
        model.addAttribute("mycode", mycode);
        model.addAttribute("activities", activities);
        return "me";
    }
   
}
