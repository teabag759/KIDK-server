package com.kidk.api.domain.family;

import com.kidk.api.domain.family.FamilyRequest;
import com.kidk.api.domain.family.FamilyResponse;
import com.kidk.api.domain.familymember.FamilyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public FamilyResponse createFamily(@RequestBody FamilyRequest.Create request) {
        Family family = familyService.createFamily(request.getUserId(), request.getFamilyName());
        return new FamilyResponse(family);
    }

    @PostMapping("/join")
    public FamilyMember joinFamily(@RequestBody FamilyRequest.Join request) {
        // FamilyMemberResponse도 만들면 좋지만 일단 Entity 반환 유지
        return familyService.joinFamily(request.getUserId(), request.getInviteCode());
    }

    @GetMapping
    public List<FamilyResponse> getFamilies() {
        return familyService.findAll().stream()
                .map(FamilyResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{familyId}")
    public FamilyResponse getFamily(@PathVariable Long familyId) {
        return new FamilyResponse(familyService.findById(familyId));
    }
}