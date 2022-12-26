package com.mtvu.identityauthorizationserver.service;

import com.mtvu.identityauthorizationserver.model.ChatUser;
import com.mtvu.identityauthorizationserver.model.UserLoginType;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import com.mtvu.identityauthorizationserver.repository.ChatUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author mvu
 * @project chat-socket
 **/
@Service
@AllArgsConstructor
public class ChatUserService implements UserDetailsService, UserDetailsPasswordService {

    private ChatUserRepository chatUserRepository;

    private PasswordEncoder passwordEncoder;

    public boolean exists(String userId) {
        return chatUserRepository.existsById(userId);
    }

    public ChatUser createUser(ChatUserDTO.Request.Create newUser,
                               UserLoginType userLoginType, boolean isActivated) {
        var chatUser = ChatUser.builder()
            .userId(newUser.userId())
            .fullName(newUser.fullName())
            .userLoginType(userLoginType)
            .password(passwordEncoder.encode(newUser.password()))
            .chatJoinRecords(new HashSet<>())
            .isActivated(isActivated)
            .avatar(newUser.avatar())
            .build();
        return chatUserRepository.save(chatUser);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        var currentUser = getUser(userId);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(userId, currentUser.getPassword(), currentUser.isActivated(),
                true, true, !currentUser.isLocked(), authorities);
    }

    public ChatUser getUser(String userId) {
        var chatUser = chatUserRepository.findById(userId);
        if (chatUser.isEmpty()) {
            throw new UsernameNotFoundException(userId);
        }
        return chatUser.get();
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        var currentUser = getUser(user.getUsername());
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        chatUserRepository.save(currentUser);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(user.getUsername(), currentUser.getPassword(), authorities);
    }
}
