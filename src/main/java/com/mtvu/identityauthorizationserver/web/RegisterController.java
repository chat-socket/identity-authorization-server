package com.mtvu.identityauthorizationserver.web;

import com.mtvu.identityauthorizationserver.exception.UserAlreadyExistAuthenticationException;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import com.mtvu.identityauthorizationserver.service.ChatUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class RegisterController {
    private ChatUserService chatUserService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping(path = "/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String handleRegister(ChatUserDTO.Request.Create userData) {
        if (chatUserService.exists(userData.userId())) {
            throw new UserAlreadyExistAuthenticationException(userData.userId());
        }
        var isActivated = false;    // Todo: Need to verify email
        chatUserService.createUser(userData, UserLoginType.PASSWORD, isActivated);
        return "redirect:/login";
    }
}
