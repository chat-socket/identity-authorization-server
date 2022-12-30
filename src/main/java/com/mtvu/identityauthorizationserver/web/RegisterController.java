package com.mtvu.identityauthorizationserver.web;

import com.mtvu.identityauthorizationserver.exception.UserAlreadyExistAuthenticationException;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import com.mtvu.identityauthorizationserver.service.ChatUserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private ChatUserService chatUserService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping(path = "/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String handleRegister(Model model, ChatUserDTO.Request.Create userData) {
        try {
            chatUserService.createUser(userData, UserLoginType.EMAIL);
            return "redirect:/login";
        } catch (UserAlreadyExistAuthenticationException e) {
            model.addAttribute("errorMessage", "User already exists");
            return "register";
        } catch (Exception e) {
            logger.error("Exception occurred when creating a new user", userData, e);
            model.addAttribute("errorMessage", "Internal error occurred, please try again!");
            return "register";
        }
    }
}
