package com.mtvu.identityauthorizationserver.service;

import com.mtvu.identityauthorizationserver.auth.UserDetailsWithPasswordService;
import com.mtvu.identityauthorizationserver.feign.UserClient;
import com.mtvu.identityauthorizationserver.model.AuthUser;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.Base64;
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
        String valueToEncode = username + ":" + password;
        var credential = "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
        var userDto = userClient.findUser("Bearer " + accessToken.getTokenValue(), credential);
        return AuthUser.builder()
                .subject(username)
                .email(username)
                .givenName(userDto.firstName())
                .familyName(userDto.lastName())
                .picture(userDto.avatar())
                .emailVerified(userDto.isActivated())
                .enabled(userDto.isActivated())
                .accountNonLocked(!userDto.isLocked())
                .build();
    }

}
