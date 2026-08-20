package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.req.InquiryCreateReq;
import com.gaguraczi.paw.domain.mypage.dto.res.InquiryRes;
import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.mypage.repository.InquiryRepository;
import com.gaguraczi.paw.domain.mypage.support.MypageCursorCodec;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;
    private static final String DIRECTORY = "inquiry";

    private final InquiryRepository inquiryRepository;
    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;

    @Transactional
    public InquiryRes create(InquiryCreateReq req, List<MultipartFile> files) {
        User user = securityUtils.currentUser();

        List<String> uploadedKeys = new ArrayList<>();
        List<String> attachmentUrls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                S3Dto uploaded;
                try {
                    uploaded = s3Utils.uploadMultipartUnderDirectory(file, DIRECTORY);
                } catch (RuntimeException e) {
                    uploadedKeys.forEach(s3Utils::deleteQuietly);
                    throw e;
                }
                uploadedKeys.add(uploaded.getKey());
                attachmentUrls.add(uploaded.getUrl());
            }
        }

        try {
            Inquiry inquiry = Inquiry.builder()
                    .user(user)
                    .inquiryType(req.inquiryType())
                    .content(req.content())
                    .attachmentUrls(attachmentUrls)
                    .build();
            inquiryRepository.save(inquiry);
            scheduleUploadCleanupOnRollback(uploadedKeys);
            return InquiryRes.from(inquiry);
        } catch (RuntimeException e) {
            uploadedKeys.forEach(s3Utils::deleteQuietly);
            throw e;
        }
    }

    private void scheduleUploadCleanupOnRollback(List<String> uploadedKeys) {
        if (uploadedKeys.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    uploadedKeys.forEach(s3Utils::deleteQuietly);
                }
            }
        });
    }

    public CursorPageRes<InquiryRes> getMyInquiries(String cursor, Integer size) {
        User user = securityUtils.currentUser();
        int pageSize = normalizeSize(size);
        MypageCursorCodec.Cursor decoded = MypageCursorCodec.decode(cursor);

        List<Inquiry> rows = inquiryRepository.findMyInquiries(
                user.getUid(),
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.id(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<Inquiry> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<InquiryRes> content = page.stream().map(InquiryRes::from).toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? MypageCursorCodec.encode(page.getLast().getCreatedAt(), page.getLast().getInquiryId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    public InquiryRes getDetail(Long inquiryId) {
        User user = securityUtils.currentUser();
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> GeneralException.of(MypageErrorCode.INQUIRY_NOT_FOUND));
        if (!inquiry.getUser().getUid().equals(user.getUid())) {
            throw GeneralException.of(MypageErrorCode.INQUIRY_NOT_FOUND);
        }
        return InquiryRes.from(inquiry);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
