package com.gaguraczi.paw.domain.mypage.entity;

import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

/** 유저별 알림 on/off 및 방해 금지 시간대 설정. 건강 이상 알림(healthAlarm)은 방해 금지 시간에도 발송 예외로 취급한다. */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_setting")
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long notificationSettingId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "todo_alarm", nullable = false)
    private Boolean todoAlarm = true;

    @Builder.Default
    @Column(name = "health_alarm", nullable = false)
    private Boolean healthAlarm = true;

    @Builder.Default
    @Column(name = "ai_analysis_alarm", nullable = false)
    private Boolean aiAnalysisAlarm = true;

    @Builder.Default
    @Column(name = "community_alarm", nullable = false)
    private Boolean communityAlarm = true;

    @Builder.Default
    @Column(name = "chat_alarm", nullable = false)
    private Boolean chatAlarm = true;

    @Builder.Default
    @Column(name = "benefit_alarm", nullable = false)
    private Boolean benefitAlarm = true;

    @Builder.Default
    @Column(name = "dnd_enabled", nullable = false)
    private Boolean dndEnabled = false;

    @Builder.Default
    @Column(name = "dnd_start", nullable = false)
    private LocalTime dndStart = LocalTime.of(22, 0);

    @Builder.Default
    @Column(name = "dnd_end", nullable = false)
    private LocalTime dndEnd = LocalTime.of(7, 0);

    public void update(
            Boolean todoAlarm,
            Boolean healthAlarm,
            Boolean aiAnalysisAlarm,
            Boolean communityAlarm,
            Boolean chatAlarm,
            Boolean benefitAlarm,
            Boolean dndEnabled,
            LocalTime dndStart,
            LocalTime dndEnd
    ) {
        if (todoAlarm != null) {
            this.todoAlarm = todoAlarm;
        }
        if (healthAlarm != null) {
            this.healthAlarm = healthAlarm;
        }
        if (aiAnalysisAlarm != null) {
            this.aiAnalysisAlarm = aiAnalysisAlarm;
        }
        if (communityAlarm != null) {
            this.communityAlarm = communityAlarm;
        }
        if (chatAlarm != null) {
            this.chatAlarm = chatAlarm;
        }
        if (benefitAlarm != null) {
            this.benefitAlarm = benefitAlarm;
        }
        if (dndEnabled != null) {
            this.dndEnabled = dndEnabled;
        }
        if (dndStart != null) {
            this.dndStart = dndStart;
        }
        if (dndEnd != null) {
            this.dndEnd = dndEnd;
        }
    }
}
