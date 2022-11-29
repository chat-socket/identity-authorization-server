package com.mtvu.identityauthorizationserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_user")
public class ChatUser {

    @Id
    @Column(name = "user_id", length = 127)
    private String userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "user_login_type", nullable = false, updatable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserLoginType userLoginType;

    @Column(name = "password")
    private String password;

    @Column(name = "avatar")
    private String avatar;

    @OneToMany(mappedBy = "chatUser")
    private Set<ChatJoinRecord> chatJoinRecords;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

}
