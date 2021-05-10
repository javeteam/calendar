package com.aspect.calendar.controllers;

import com.aspect.calendar.entity.exceptions.AuthenticationRequiredException;
import com.aspect.calendar.entity.exceptions.InvalidValueException;
import com.aspect.calendar.entity.user.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@Controller
public class PlainController {
    @ExceptionHandler({AuthenticationRequiredException.class})
    public String handleAuthException(HttpServletResponse response){
        response.setStatus(401);
        return "redirect:/login";
    }

    @ExceptionHandler({InvalidValueException.class})
    public String handleInvalidValueException(HttpServletResponse response, Exception ex){
        response.setStatus(400);
        return "{\"status\":\"Error\",\"message\":\"" + ex.getMessage() + "\"}";
    }

    public AppUser getAuthenticatedUser() throws AuthenticationRequiredException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) throw new AuthenticationRequiredException("User is not authenticated");
        AppUser user = (AppUser) authentication.getPrincipal();
        if(user.getId() == 0) throw new AuthenticationRequiredException("User is not authenticated");

        return user;
    }
}
