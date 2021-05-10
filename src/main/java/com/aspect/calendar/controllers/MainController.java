package com.aspect.calendar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class MainController {

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String welcome() {
        return "redirect:/calendar";
    }

    @RequestMapping(value = {"/login","/logoutSuccessful"})
    public String loginPage(@RequestParam (value = "error", required = false) String error, Model model) {
        if (error != null && error.equals("true")){
            String message = "В авторизації відмовлено. Хибні дані.";
            model.addAttribute("message", message);
        }
        return "login";
    }

}
