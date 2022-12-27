package com.mtvu.identityauthorizationserver.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * @author mvu
 * @project chat-socket
 **/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "chat_join_record")
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

    @Column(name = "role_id", nullable = false, updatable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private GroupRole role;
}
