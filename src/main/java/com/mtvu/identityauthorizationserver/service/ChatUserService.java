package com.mtvu.identityauthorizationserver.service;

import com.mtvu.identityauthorizationserver.exception.UserAlreadyExistAuthenticationException;
import com.mtvu.identityauthorizationserver.model.ChatUser;
import com.mtvu.identityauthorizationserver.record.ChatUserDTO;
import com.mtvu.identityauthorizationserver.repository.ChatUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatUserService implements UserDetailsService {

    private ChatUserRepository chatUserRepository;

    private PasswordEncoder passwordEncoder;

    public boolean exists(String userId) {
        return chatUserRepository.existsById(userId);
    }

    public void createUser(ChatUserDTO.Request.Create newUser) {
        if (exists(newUser.userId())) {
            throw new UserAlreadyExistAuthenticationException(newUser.userId());
        }
        var chatUser = ChatUser.builder()
            .userId(newUser.userId())
            .fullName(newUser.fullName())
            .userLoginType(newUser.userLoginType())
            .password(passwordEncoder.encode(newUser.password()))
            .chatJoinRecords(new HashSet<>())
            .avatar(newUser.avatar())
            .build();
        chatUserRepository.save(chatUser);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        var chatUser = chatUserRepository.findById(userId);
        if (chatUser.isEmpty()) {
            throw new UsernameNotFoundException(userId);
        }

        var currentUser = chatUser.get();
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(userId, currentUser.getPassword(), authorities);
    }
}
