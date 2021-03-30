package com.aspect.calendar.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


public class WebUtils {

    public static String toString(User user){
        StringBuilder sb = new StringBuilder();

        sb.append("login:").append(user.getUsername());
        Collection<GrantedAuthority> authorities = user.getAuthorities();
        if(authorities != null && !authorities.isEmpty()){
            sb.append(" (");
            boolean first = true;
            for (GrantedAuthority ga : authorities){
                if(first) {
                    sb.append(ga.getAuthority());
                    first = false;
                } else sb.append(", ").append(ga.getAuthority());
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
