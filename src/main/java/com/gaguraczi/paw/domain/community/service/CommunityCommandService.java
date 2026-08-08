package com.gaguraczi.paw.domain.community.service;

import com.gaguraczi.paw.domain.community.dto.req.CommunityCreateReq;
import com.gaguraczi.paw.domain.community.dto.req.CommunityUpdateReq;
import com.gaguraczi.paw.domain.community.dto.res.CommunityDetailRes;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.entity.CommunityPhoto;
import com.gaguraczi.paw.domain.community.entity.CommunityTag;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.domain.community.redis.CommunityCountRedisStore;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.community.repository.CommunityTagRepository;
import com.gaguraczi.paw.domain.community.support.CommunityImageValidator;
import com.gaguraczi.paw.domain.like.repository.CommunityLikeRepository;
import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.region.repository.LegalRegionRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityCommandService {

    private static final Set<PostType> SUPPORTED = EnumSet.of(PostType.COMMUNICATION, PostType.MARKET);

    private final CommunityRepository communityRepository;
    private final CommunityTagRepository communityTagRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final LegalRegionRepository legalRegionRepository;
    private final CommunityCountRedisStore communityCountRedisStore;
    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;

    @Transactional
    public CommunityDetailRes create(CommunityCreateReq req, List<MultipartFile> images) {
        if (req.postType() == null || !SUPPORTED.contains(req.postType())) {
            throw GeneralException.of(CommunityErrorCode.POST_TYPE_UNSUPPORTED_400);
        }
        User user = securityUtils.currentUser();
        CommunityTag tag = loadActiveTag(req.tagCode(), req.postType());
        LegalRegion region = resolveRegion(req.postType(), req.regionCode());
        validateMarketCreate(req);

        List<MultipartFile> files = normalizeFiles(images);
        validatePhotoCount(files.size());
        List<S3Dto> uploaded = uploadAll(files);

        try {
            Community community = Community.builder()
                    .user(user)
                    .postType(req.postType())
                    .communityTag(tag)
                    .title(req.title())
                    .content(req.content())
                    .hashTags(req.hashTags() == null ? new ArrayList<>() : new ArrayList<>(req.hashTags()))
                    .tradeType(req.postType() == PostType.MARKET ? req.tradeType() : null)
                    .marketStatus(req.postType() == PostType.MARKET ? MarketStatus.IN_PROGRESS : null)
                    .price(req.postType() == PostType.MARKET ? req.price() : null)
                    .priceNegotiable(Boolean.TRUE.equals(req.priceNegotiable()))
                    .expiryDate(req.postType() == PostType.MARKET ? req.expiryDate() : null)
                    .tradeMethod(req.postType() == PostType.MARKET ? req.tradeMethod() : null)
                    .region(region)
                    .build();

            List<CommunityPhoto> photos = toPhotos(uploaded);
            community.replacePhotos(photos);
            community.applyThumbnailIndex(req.thumbnailIndex());
            communityRepository.save(community);

            return CommunityDetailRes.from(
                    community,
                    community.getViewCount(),
                    community.getLikeCount(),
                    false
            );
        } catch (RuntimeException e) {
            uploaded.forEach(u -> s3Utils.deleteQuietly(u.getKey()));
            throw e;
        }
    }

    @Transactional
    public CommunityDetailRes update(Long postId, CommunityUpdateReq req, List<MultipartFile> images) {
        Community community = communityRepository.findDetailById(postId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMUNITY_NOT_FOUND_404));
        assertAuthor(community);
        if (!SUPPORTED.contains(community.getPostType())) {
            throw GeneralException.of(CommunityErrorCode.POST_TYPE_UNSUPPORTED_400);
        }

        CommunityTag tag = loadActiveTag(req.tagCode(), community.getPostType());
        LegalRegion region = resolveRegion(community.getPostType(), req.regionCode());
        if (community.getPostType() == PostType.MARKET && req.tradeType() == null) {
            throw GeneralException.of(CommunityErrorCode.MARKET_FIELD_REQUIRED_400);
        }

        List<String> keepUrls = req.keepPhotoUrls() == null ? List.of() : req.keepPhotoUrls();
        List<MultipartFile> files = normalizeFiles(images);
        validatePhotoCount(keepUrls.size() + files.size());

        List<CommunityPhoto> removed = new ArrayList<>();
        Set<String> keepSet = new HashSet<>(keepUrls);
        for (CommunityPhoto photo : community.getPhotos()) {
            if (!keepSet.contains(photo.getPhotoS3Url())) {
                removed.add(photo);
            }
        }

        List<S3Dto> uploaded = uploadAll(files);
        try {
            community.updateContent(
                    tag,
                    req.title(),
                    req.content(),
                    req.hashTags(),
                    community.getPostType() == PostType.MARKET ? req.tradeType() : null,
                    community.getPostType() == PostType.MARKET
                            ? (req.marketStatus() == null ? community.getMarketStatus() : req.marketStatus())
                            : null,
                    community.getPostType() == PostType.MARKET ? req.price() : null,
                    req.priceNegotiable(),
                    community.getPostType() == PostType.MARKET ? req.expiryDate() : null,
                    community.getPostType() == PostType.MARKET ? req.tradeMethod() : null,
                    region
            );

            community.getPhotos().removeIf(photo -> !keepSet.contains(photo.getPhotoS3Url()));
            for (CommunityPhoto photo : toPhotos(uploaded)) {
                photo.bindCommunity(community);
                photo.changeSortOrder(community.getPhotos().size());
                community.getPhotos().add(photo);
            }
            for (int i = 0; i < community.getPhotos().size(); i++) {
                community.getPhotos().get(i).changeSortOrder(i);
            }

            if (req.thumbnailUrl() != null && !req.thumbnailUrl().isBlank()) {
                community.ensureSingleThumbnail(req.thumbnailUrl());
            } else {
                community.applyThumbnailIndex(req.thumbnailIndex());
            }

            List<String> removedKeys = removed.stream()
                    .map(CommunityPhoto::getPhotoKey)
                    .filter(Objects::nonNull)
                    .toList();
            afterCommit(() -> removedKeys.forEach(s3Utils::deleteQuietly));

            UUID uid = securityUtils.currentUid();
            boolean liked = communityLikeRepository.findByCommunity_PostIdAndUser_Uid(postId, uid).isPresent();
            return CommunityDetailRes.from(
                    community,
                    communityCountRedisStore.getViewCount(community),
                    communityCountRedisStore.getLikeCount(community),
                    liked
            );
        } catch (RuntimeException e) {
            uploaded.forEach(u -> s3Utils.deleteQuietly(u.getKey()));
            throw e;
        }
    }

    @Transactional
    public void delete(Long postId) {
        Community community = communityRepository.findDetailById(postId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMUNITY_NOT_FOUND_404));
        assertAuthor(community);

        List<String> keys = community.getPhotos().stream()
                .map(CommunityPhoto::getPhotoKey)
                .filter(Objects::nonNull)
                .toList();
        communityRepository.delete(community);
        afterCommit(() -> {
            communityCountRedisStore.deleteCounts(postId);
            keys.forEach(s3Utils::deleteQuietly);
        });
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private void assertAuthor(Community community) {
        UUID uid = securityUtils.currentUid();
        if (!Objects.equals(community.getUser().getUid(), uid)) {
            throw GeneralException.of(CommunityErrorCode.FORBIDDEN_403);
        }
    }

    private CommunityTag loadActiveTag(CommunityTagCode tagCode, PostType postType) {
        if (tagCode == null) {
            throw GeneralException.of(CommunityErrorCode.TAG_NOT_FOUND_404);
        }
        if (tagCode.getPostType() != postType) {
            throw GeneralException.of(CommunityErrorCode.TAG_TYPE_MISMATCH_400);
        }
        CommunityTag tag = communityTagRepository.findByPostTypeAndTagCode(postType, tagCode.name())
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.TAG_NOT_FOUND_404));
        if (!Boolean.TRUE.equals(tag.getIsActive())) {
            throw GeneralException.of(CommunityErrorCode.TAG_NOT_FOUND_404);
        }
        return tag;
    }

    private LegalRegion resolveRegion(PostType postType, String regionCode) {
        if (postType != PostType.MARKET) {
            return null;
        }
        if (regionCode == null || regionCode.isBlank()) {
            return null;
        }
        return legalRegionRepository.findById(regionCode)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.REGION_NOT_FOUND_404));
    }

    private void validateMarketCreate(CommunityCreateReq req) {
        if (req.postType() != PostType.MARKET) {
            return;
        }
        if (req.tradeType() == null || req.tradeMethod() == null) {
            throw GeneralException.of(CommunityErrorCode.MARKET_FIELD_REQUIRED_400);
        }
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        List<MultipartFile> files = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                CommunityImageValidator.validate(image);
                files.add(image);
            }
        }
        return files;
    }

    private void validatePhotoCount(int count) {
        if (count > CommunityImageValidator.MAX_PHOTOS) {
            throw GeneralException.of(CommunityErrorCode.PHOTO_LIMIT_400);
        }
    }

    private List<S3Dto> uploadAll(List<MultipartFile> files) {
        List<S3Dto> uploaded = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                uploaded.add(s3Utils.uploadMultipartUnderDirectory(file, "community"));
            }
            return uploaded;
        } catch (RuntimeException e) {
            uploaded.forEach(u -> s3Utils.deleteQuietly(u.getKey()));
            throw e;
        }
    }

    private List<CommunityPhoto> toPhotos(List<S3Dto> uploaded) {
        List<CommunityPhoto> photos = new ArrayList<>();
        for (S3Dto dto : uploaded) {
            photos.add(CommunityPhoto.builder()
                    .photoS3Url(dto.getUrl())
                    .photoKey(dto.getKey())
                    .isThumbnail(false)
                    .sortOrder(0)
                    .build());
        }
        return photos;
    }
}
