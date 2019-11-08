package com.vkfriends.task;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService{
    private User user;

    public void setUser(String login, String password){
        user = new User(login, password);
    }

    public User getUser(){
        return user;
    }

}
