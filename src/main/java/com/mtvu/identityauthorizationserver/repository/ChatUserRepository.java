package com.mtvu.identityauthorizationserver.repository;

import com.mtvu.identityauthorizationserver.model.ChatUser;
import org.springframework.data.repository.CrudRepository;

public interface ChatUserRepository extends CrudRepository<ChatUser, String> {
}
