package com.mtvu.identityauthorizationserver.api;

import com.mtvu.identityauthorizationserver.model.ChatJoinRecord;
import com.mtvu.identityauthorizationserver.model.ChatUserGroupKey;
import com.mtvu.identityauthorizationserver.model.GroupRole;
import com.mtvu.identityauthorizationserver.record.ChatGroupDTO;
import com.mtvu.identityauthorizationserver.service.ChatGroupService;
import com.mtvu.identityauthorizationserver.service.ChatUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.ResponseEntity.notFound;

/**
 * @author mvu
 * @project chat-socket
 **/
@AllArgsConstructor
@RestController("/api/group")
public class GroupManagementController {

    private final ChatGroupService chatGroupService;

    private final ChatUserService chatUserService;

    @GetMapping("/{groupId}")
    public ResponseEntity<ChatGroupDTO.Response.Public> getGroup(@PathVariable("groupId") String groupId) {
        var group = chatGroupService.getChatGroup(groupId);
        return group
            .map((x) -> ResponseEntity.ok(ChatGroupDTO.Response.Public.create(x)))
            .orElseGet(() -> notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<ChatGroupDTO.Response.Public> createGroup(
            @RequestBody ChatGroupDTO.Request.Create data, Principal principal) {
        var chatGroup = chatGroupService.createChatGroup(data);
        Set<ChatJoinRecord> chatJoinRecords = new HashSet<>();
        if (!data.participants().contains(principal.getName())) {
            // The current user is supposed to be inside the list of participants
            return ResponseEntity.badRequest().build();
        }
        for (String participant : data.participants()) {
            var role = participant.equals(principal.getName()) ? GroupRole.ADMIN : GroupRole.MEMBER;
            var chatUser = chatUserService.getUser(participant);
            chatJoinRecords.add(ChatJoinRecord.builder()
                    .id(new ChatUserGroupKey(chatGroup.getGroupId(), chatUser.getUserId()))
                    .chatGroup(chatGroup)
                    .chatUser(chatUser)
                    .role(role)
                    .build()
            );
        }
        chatGroupService.addChatMembers(chatGroup, chatJoinRecords);
        return ResponseEntity.ok(ChatGroupDTO.Response.Public.create(chatGroup));
    }
}
