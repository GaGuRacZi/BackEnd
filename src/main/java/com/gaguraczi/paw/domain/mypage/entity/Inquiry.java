package com.gaguraczi.paw.domain.mypage.entity;

import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.converter.StringListConverter;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "inquiry",
        indexes = @Index(name = "idx_inquiry_uid_created", columnList = "uid, created_at")
)
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type", nullable = false, length = 30)
    private InquiryType inquiryType;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private List<String> attachmentUrls = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.RECEIVED;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    public void answer(String answer) {
        this.answer = answer;
        this.status = InquiryStatus.ANSWERED;
    }
}
