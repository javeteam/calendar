package com.aspect.calendar.entity.user;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class AppUser extends Person implements UserDetails {
    private String username;
    private String password;
    private List<UserRole> authorities;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(List<UserRole> authorities) {
        this.authorities = authorities;
    }

    public boolean hasRole(String roleName){
        for(UserRole role : authorities ){
            if(role.getAuthority().equals(roleName) || role.getAuthority().equals("ROLE_" + roleName)) return true;
        }
        return false;
    }

    @Override
    public List<UserRole> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive();
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }
}
