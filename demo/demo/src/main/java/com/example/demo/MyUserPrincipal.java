package com.example.demo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MyUserPrincipal implements UserDetails {
    
    private User user;

    @Override
    public Collection<GrantedAuthority> getAuthorities() {

        final Set<GrantedAuthority> auths = new HashSet<GrantedAuthority>();

        auths.add(new SimpleGrantedAuthority(user.getRole()));

        return auths;
        
    } 
         
    public MyUserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
            return user.getPassword();
    }
    
    @Override
    public String getUsername() {
            if (this.user == null) {
                    return null;
            }
            return this.user.getUsername();
    }
    
    @Override
    public boolean isEnabled() {
        if (this.user == null) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean isAccountNonExpired() {
            return !this.user.isAccountExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
            return !this.user.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
            return !this.user.isCredentialsExpired();
    }


}