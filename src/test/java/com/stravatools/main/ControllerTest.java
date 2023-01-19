package com.stravatools.main;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void indexShouldReturn200OK() throws Exception {
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(containsString("Register to link Strava account")));
    }

    @Test
    public void loginShouldReturn200OK() throws Exception {
        this.mockMvc.perform(get("/login")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome Back, please login")));
    }

    @Test
    public void unauthenticatedUsersShouldGetRedirect() throws Exception {
        this.mockMvc.perform(get("/stravaauth")).andExpect(status().is3xxRedirection());

        this.mockMvc.perform(get("/me/activities")).andExpect(status().is3xxRedirection());

        this.mockMvc.perform(get("/me/defaultbikes")).andExpect(status().is3xxRedirection());

        this.mockMvc.perform(get("/me/maintenance")).andExpect(status().is3xxRedirection());

        this.mockMvc.perform(get("/me/muting")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    public void getToStravaAuthWithUser() throws Exception {
        this.mockMvc.perform(get("/stravaauth")).andDo(print()).andExpect(status().isOk())
        .andExpect(content().string(containsString("Click link to redirect and link Strava account")));
    }

    
}
