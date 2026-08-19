package com.gaguraczi.paw.utils.S3;

import com.gaguraczi.paw.global.config.AmazonConfig;
import com.gaguraczi.paw.utils.exception.UtilException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.function.Consumer;


import static com.gaguraczi.paw.utils.exception.UtilException.Reason.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {


    private final S3Client s3Client;
    private final AmazonConfig config;



    // 파일 키 생성 (경로 구분자·특수문자 제거 후 UUID와 결합)
    private String generateFileKey(String fileName) {
        String baseName = fileName.replace('\\', '/');
        int slash = baseName.lastIndexOf('/');
        if (slash >= 0) {
            baseName = baseName.substring(slash + 1);
        }
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isBlank() || ".".equals(sanitized) || "..".equals(sanitized)) {
            return UUID.randomUUID().toString();
        }
        return UUID.randomUUID() + "-" + sanitized;
    }

    private String getUrl(String key) {
        String cdnUrl = config.getCdnUrl();
        if (cdnUrl == null || cdnUrl.isBlank()) {
            return "https://" + config.getBucket()
                    + ".s3." + config.getRegion()
                    + ".amazonaws.com/" + key;
        }
        return cdnUrl.endsWith("/") ? cdnUrl + key : cdnUrl + "/" + key;
    }

    // MultipartFile 그대로 업로드 (범용)
    public S3Dto uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new UtilException(FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new UtilException(FILE_EMPTY);
        }

        String key = generateFileKey(originalFilename);
        return uploadMultipartUsingKey(file, key);
    }

    // 디렉터리 prefix 하위 경로(UUID 파일명+확장자)로 업로드합니다.
    public S3Dto uploadMultipartUnderDirectory(MultipartFile file, String directoryPrefix) {

        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new UtilException(FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new UtilException(FILE_EMPTY);
        }

        String prefix = normalizeDirectoryPrefix(directoryPrefix);

        String baseName = originalFilename.replace('\\', '/');
        int slash = baseName.lastIndexOf('/');
        if (slash >= 0) {
            baseName = baseName.substring(slash + 1);
        }

        String ext = "";
        int dot = baseName.lastIndexOf('.');
        if (dot > 0 && dot < baseName.length() - 1) {
            // 확장자만 소문자화하고 URL/키 안전 문자로 sanitize (# 등 제거)
            ext = baseName.substring(dot).toLowerCase().replaceAll("[^a-z0-9._-]", "_");
        }

        String key = prefix + UUID.randomUUID() + ext;
        return uploadMultipartUsingKey(file, key);
    }

    /** null/blank·?, #, \\ 및 비정상 path segment를 거부한 뒤 trailing slash를 보장합니다. */
    private String normalizeDirectoryPrefix(String directoryPrefix) {
        if (directoryPrefix == null || directoryPrefix.isBlank()) {
            throw new UtilException(TYPE_NOT_ALLOWED);
        }
        if (directoryPrefix.indexOf('?') >= 0
                || directoryPrefix.indexOf('#') >= 0
                || directoryPrefix.indexOf('\\') >= 0) {
            throw new UtilException(TYPE_NOT_ALLOWED);
        }

        String trimmed = directoryPrefix.endsWith("/")
                ? directoryPrefix.substring(0, directoryPrefix.length() - 1)
                : directoryPrefix;
        if (trimmed.isEmpty()) {
            throw new UtilException(TYPE_NOT_ALLOWED);
        }

        for (String segment : trimmed.split("/", -1)) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                throw new UtilException(TYPE_NOT_ALLOWED);
            }
        }

        return trimmed + "/";
    }

    private S3Dto uploadMultipartUsingKey(MultipartFile file, String key) {
        String contentType = (file.getContentType() != null)
                ? file.getContentType()
                : "application/octet-stream";

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        try (InputStream is = file.getInputStream()) {
            s3Client.putObject(req, RequestBody.fromInputStream(is, file.getSize()));
            return new S3Dto(getUrl(key), key);
        } catch (Exception e) {
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }

    /** 가공된 바이트 업로드 (프로필 리사이즈 결과 등) Image/ImageUtill 클래스와 조합해서 사용할 것! */
    public S3Dto uploadBytes(byte[] bytes, String fileName, String contentType) {

        if (bytes == null || bytes.length == 0) {
            throw new UtilException(FILE_EMPTY);
        }

        if (fileName == null || fileName.isBlank()) {
            throw new UtilException(FILE_EMPTY);
        }

        String key = generateFileKey(fileName);

        String ct = (contentType != null && !contentType.isBlank())
                ? contentType
                : "application/octet-stream";

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(ct)
                .build();

        try {
            s3Client.putObject(req, RequestBody.fromBytes(bytes));
            return new S3Dto(getUrl(key), key);   // (url, key) 순서 통일
        } catch (Exception e) {
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }

    public void deleteFile(String key) {

        if (key == null || key.isBlank()) {
            throw new UtilException(S3_DELETE_FAILED);
        }

        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(req);
            log.info("Deleted file from S3: {}", key);
        } catch (Exception e) {
            throw new UtilException(S3_DELETE_FAILED, e);
        }
    }

    /**
     * Uploads a new profile image, applies the domain update, and schedules S3 cleanup:
     * previous key is deleted after commit; uploaded key is deleted if the transaction rolls back.
     * If the domain update fails immediately, the uploaded key is deleted synchronously.
     */
    public void replaceUnderDirectory(
            MultipartFile file,
            String directoryPrefix,
            String previousKey,
            Consumer<S3Dto> applyUploaded
    ) {
        S3Dto uploaded = uploadMultipartUnderDirectory(file, directoryPrefix);
        try {
            applyUploaded.accept(uploaded);
        } catch (RuntimeException e) {
            deleteQuietly(uploaded.getKey());
            throw e;
        }
        scheduleReplaceCleanup(previousKey, uploaded.getKey());
    }

    private void scheduleReplaceCleanup(String previousKey, String uploadedKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(previousKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(previousKey);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteQuietly(uploadedKey);
                }
            }
        });
    }

    /**
     * Deletes the key only after the current transaction commits, so a rolled-back
     * DB change never leaves the S3 object deleted while still referenced.
     */
    public void scheduleDeleteAfterCommit(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(key);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(key);
            }
        });
    }

    public void deleteQuietly(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            deleteFile(key);
        } catch (Exception ex) {
            log.warn("Failed to cleanup S3 object: {}", key, ex);
        }
    }

    public Path downloadToTempFile(String key) {
        if (key == null || key.isBlank()) {
            throw new UtilException(FILE_EMPTY);
        }
        Path temp = null;
        try {
            String suffix = ".audio";
            int slash = key.lastIndexOf('/');
            String name = slash >= 0 ? key.substring(slash + 1) : key;
            int dot = name.lastIndexOf('.');
            if (dot > 0 && dot < name.length() - 1) {
                suffix = name.substring(dot);
            }
            temp = Files.createTempFile("visit-audio-", suffix);
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(key)
                    .build();
            try (InputStream in = s3Client.getObject(req)) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            return temp;
        } catch (Exception e) {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (Exception ignored) {
                    // temp cleanup best-effort
                }
            }
            throw new UtilException(S3_DOWNLOAD_FAILED, e);
        }
    }
}
