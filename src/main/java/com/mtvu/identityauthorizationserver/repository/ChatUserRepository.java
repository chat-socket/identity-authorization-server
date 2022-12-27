package com.mtvu.identityauthorizationserver.repository;

import com.mtvu.identityauthorizationserver.model.ChatUser;
import org.springframework.data.repository.CrudRepository;

/**
 * @author mvu
 * @project chat-socket
 **/
public interface ChatUserRepository extends CrudRepository<ChatUser, String> {
}
