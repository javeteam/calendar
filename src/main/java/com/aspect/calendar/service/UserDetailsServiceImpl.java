package com.aspect.calendar.service;

import com.aspect.calendar.dao.UserDetailsDao;
import com.aspect.calendar.entity.enums.Division;
import com.aspect.calendar.entity.user.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<List<Person>> getProvidersSeparateByDivisions(){
        List<List<Person>> divisions = new ArrayList<>();
        List<Person> providers = getAllActiveProviders();

        if(providers.size() > 0){
            Division divisionName = providers.get(0).getDivision();
            List<Person> divisionProviders = new ArrayList<>();
            for (Person person : providers){
                if(person.getDivision() != divisionName){
                    divisions.add(divisionProviders);
                    divisionProviders = new ArrayList<>();
                    divisionName = person.getDivision();
                }
                divisionProviders.add(person);
            }
            divisions.add(divisionProviders);
        }

        return divisions;
    }

}
