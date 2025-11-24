package com.kidk.api.domain.family;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class FamilyResponse {
    private Long id;
    private String familyName;
    private String inviteCode;
    private LocalDateTime onCreate;
    private LocalDateTime onUpdate;

    public FamilyResponse(Family family) {
        this.id = family.getId();
        this.familyName = family.getFamilyName();
        this.inviteCode = family.getInviteCode();
        this.onCreate = family.getCreatedAt();
        this.onUpdate = family.getUpdatedAt();
    }

}
