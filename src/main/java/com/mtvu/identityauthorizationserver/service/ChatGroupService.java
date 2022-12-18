package com.mtvu.identityauthorizationserver.service;

import com.mtvu.identityauthorizationserver.model.ChatGroup;
import com.mtvu.identityauthorizationserver.repository.ChatGroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author mvu
 * @project chat-socket
 **/
@Service
@AllArgsConstructor
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;

    public Optional<ChatGroup> getChatGroup(String groupId) {
        return chatGroupRepository.findById(groupId);
    }
}
