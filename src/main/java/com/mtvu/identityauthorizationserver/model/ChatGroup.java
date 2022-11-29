package com.mtvu.identityauthorizationserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_group")
public class ChatGroup {

    @Id
    @Column(name = "group_id", nullable = false, length = 127)
    private String groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "group_description", nullable = false, length = 1000)
    private String groupDescription;

    @Column(name = "group_avatar")
    private String groupAvatar;

    @OneToMany(mappedBy = "chatGroup")
    private Set<ChatJoinRecord> chatJoinRecords;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

}
