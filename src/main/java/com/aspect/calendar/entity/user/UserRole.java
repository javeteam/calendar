package com.aspect.calendar.entity.user;

import org.springframework.security.core.GrantedAuthority;

public class UserRole implements GrantedAuthority {
    private final String authority;

    public UserRole(String authority){
        if(authority == null) this.authority = "ROLE_USER";
        else if(authority.toUpperCase().indexOf("ROLE_") == 0) this.authority = authority.toUpperCase();
        else this.authority = "ROLE_" + authority.toUpperCase();
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
