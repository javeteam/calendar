package com.aspect.calendar.service;

import com.aspect.calendar.dao.UserDetailsDao;
import com.aspect.calendar.entity.user.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDetailsDao userDetailsDAO;

    @Autowired
    UserDetailsServiceImpl(UserDetailsDao userDetailsDAO){
        this.userDetailsDAO = userDetailsDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        return this.userDetailsDAO.findUserAccount(username);
    }

    public Person getPersonById(int id){
        return this.userDetailsDAO.get(id);
    }

    public List<Person> getAllActiveManagers(){
        return this.userDetailsDAO.getAllActiveManagers();
    }

    public List<Person> getAllActiveProviders(){
        return this.userDetailsDAO.getActiveProviders();
    }

}
