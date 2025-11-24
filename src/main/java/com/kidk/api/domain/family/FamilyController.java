package com.kidk.api.domain.family;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    // 전체 조회 (테스트용)
    @GetMapping
    public List<Family> getFamilies() {
        return familyService.findAll();
    }

    @GetMapping("/{familyId}")
    public Family getFamily(@PathVariable Long familyId) {
        return familyService.findById(familyId);
    }


}
