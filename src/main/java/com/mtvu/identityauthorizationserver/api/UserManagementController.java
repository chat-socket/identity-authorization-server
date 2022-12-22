package com.mtvu.identityauthorizationserver.api;

import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import com.mtvu.identityauthorizationserver.service.ChatUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mvu
 * @project chat-socket
 **/
@AllArgsConstructor
@RestController("/user")
public class UserManagementController {

    private ChatUserService chatUserService;

    @PostMapping("/register")
    public ResponseEntity<ChatUserDTO.Response.Public> register(@RequestBody ChatUserDTO.Request.Create userData) {
        // Todo: verify the given email address by sending an confirmation email
        var newUser = chatUserService.createUser(userData, UserLoginType.PASSWORD);
        return ResponseEntity.ok(ChatUserDTO.Response.Public.create(newUser));
    }
}
