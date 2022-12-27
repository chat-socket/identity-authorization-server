package com.mtvu.identityauthorizationserver.service;

import com.mtvu.identityauthorizationserver.auth.UserDetailsWithPasswordService;
import com.mtvu.identityauthorizationserver.feign.UserClient;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author mvu
 * @project chat-socket
 **/
@Service
@AllArgsConstructor
public class ChatUserService implements UserDetailsWithPasswordService {

    private UserClient userClient;

    private OAuth2Service oAuth2Service;

    public ChatUserDTO.Response.Public createUser(ChatUserDTO.Request.Create newUser,
                                                  UserLoginType userLoginType) {
        var accessToken = oAuth2Service.getDefaultClientAccessToken();
        return userClient.register("Bearer " + accessToken.getTokenValue(), userLoginType, newUser);
    }

    @Override
    public UserDetails loadUserByUsernameAndPassword(String username, String password) {
        var accessToken = oAuth2Service.getDefaultClientAccessToken();
        var userDto = userClient.findUser("Bearer " + accessToken.getTokenValue(), username, password);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(userDto.userId(), "", userDto.isActivated(), true, true,
                !userDto.isLocked(), authorities);
    }
}
