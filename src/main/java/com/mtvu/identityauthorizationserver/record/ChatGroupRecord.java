package com.mtvu.identityauthorizationserver.record;

import com.mtvu.identityauthorizationserver.model.ChatGroup;
import com.mtvu.identityauthorizationserver.model.ChatUser;
import com.mtvu.identityauthorizationserver.model.ChatJoinRecord;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ChatGroupRecord (String groupId, String name, String avatar, Set<String> participants,
                               OffsetDateTime createdAt) {
    public ChatGroupRecord(ChatGroup chatGroup) {
        this(chatGroup.getGroupId(),
            chatGroup.getGroupName(),
            chatGroup.getGroupAvatar(),
            chatGroup.getChatJoinRecords().stream()
                .map(ChatJoinRecord::getChatUser)
                .map(ChatUser::getUserId)
                .collect(Collectors.toSet()),
            chatGroup.getCreatedAt());
    }
}
