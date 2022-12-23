package com.mtvu.identityauthorizationserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * @author mvu
 * @project chat-socket
 **/
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatUserGroupKey implements Serializable {

    @Column(name = "group_id", length = 127)
    private String groupId;

    @Column(name = "user_id", length = 127)
    private String userId;
}
