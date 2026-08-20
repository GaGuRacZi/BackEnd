package com.gaguraczi.paw.domain.walk.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkInProgressSession {

    private Long petId;
    private LocalDateTime startTime;
    private LocalDate walkDate;

    @Builder.Default
    private boolean processing = false;
}
