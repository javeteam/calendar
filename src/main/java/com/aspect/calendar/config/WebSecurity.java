package com.aspect.calendar.config;

import com.aspect.calendar.entity.user.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class WebSecurity {
    public AppUser getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) return null;
        return (AppUser) authentication.getPrincipal();
    }

    public boolean hasRole(String roleName){
        if(getAuthenticatedUser() == null) return false;
        return getAuthenticatedUser().hasRole(roleName);
    }

    public boolean hasRoles(String ... roleNames){
        if(getAuthenticatedUser() == null) return false;
        for(String roleName : roleNames){
            if(getAuthenticatedUser().hasRole(roleName)) return true;
        }
        return false;
    }

}
