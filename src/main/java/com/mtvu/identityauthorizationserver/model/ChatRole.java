package com.mtvu.identityauthorizationserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class ChatRole {

    @Id
    @Column(length = 100)
    private String roleId;

    @Column(length = 1000)
    private String roleDescription;
}
