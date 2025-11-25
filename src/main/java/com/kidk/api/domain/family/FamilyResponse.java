package com.kidk.api.domain.family;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class FamilyResponse {
    private final Long id;
    private final String familyName;
    private final String inviteCode;
    private final LocalDateTime onCreate;

    public FamilyResponse(Family family) {
        this.id = family.getId();
        this.familyName = family.getFamilyName();
        this.inviteCode = family.getInviteCode();
        this.onCreate = family.getCreatedAt();
    }

}
