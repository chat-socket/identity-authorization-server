package com.mtvu.identityauthorizationserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
public class ChatJoinRecord {

    @EmbeddedId
    private ChatUserGroupKey id;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private ChatGroup chatGroup;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private ChatUser chatUser;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime joinedAt;

    @ManyToOne
    @JoinColumn(name = "roleId")
    private ChatRole role;
}
