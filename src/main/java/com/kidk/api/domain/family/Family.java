package com.kidk.api.domain.family;

import com.kidk.api.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="families")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Family extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_name", length = 100)
    private String familyName;
}
