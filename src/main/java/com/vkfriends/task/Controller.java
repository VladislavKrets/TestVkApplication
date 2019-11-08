package com.vkfriends.task;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@org.springframework.stereotype.Controller
public class Controller {

    @GetMapping("/")
    public String authenticate(){
        return "auth";
    }

    @PostMapping("/")
    public String postAuthenticate(Model model){
        String login = (String) model.getAttribute("j_login");
        String password = (String) model.getAttribute("j_password");

        System.out.println(login + " " + password);
        
        return "auth";
    }

}
