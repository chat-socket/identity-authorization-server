package com.mtvu.identityauthorizationserver.repository;

import com.mtvu.identityauthorizationserver.model.ChatGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author mvu
 * @project chat-socket
 **/
@Repository
public interface ChatGroupRepository extends CrudRepository<ChatGroup, String> {
}
