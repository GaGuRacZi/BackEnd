package com.gaguraczi.paw.domain.visit.support;

import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class VisitAudioValidator {

    private static final Tika TIKA = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/x-m4a",
            "audio/m4a",
            "audio/aac",
            "audio/x-aac",
            "audio/mp4a-latm"
    );

    private static final Set<String> REJECTED_MIME_TYPES = Set.of(
            "audio/wav",
            "audio/x-wav",
            "audio/wave",
            "audio/vnd.wave",
            "audio/x-pn-wav"
    );

    private final VisitProperties visitProperties;

    public int validateAndDurationSec(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_REQUIRED);
        }
        if (file.getSize() > visitProperties.getMaxAudioBytes()) {
            throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_TOO_LARGE);
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (filename.endsWith(".wav") || filename.endsWith(".wave")) {
            throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_TYPE);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_TYPE, e);
        }
        String detectedType = TIKA.detect(bytes, file.getOriginalFilename());
        String mime = detectedType == null ? "" : detectedType.toLowerCase(Locale.ROOT);
        if (REJECTED_MIME_TYPES.contains(mime) || !ALLOWED_MIME_TYPES.contains(mime)) {
            throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_TYPE);
        }

        int durationSec = readDurationSec(bytes, file.getOriginalFilename());
        if (durationSec > visitProperties.getMaxAudioDurationSec()) {
            throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_DURATION);
        }
        return durationSec;
    }

    private static int readDurationSec(byte[] bytes, String originalFilename) {
        Path temp = null;
        try {
            String suffix = ".audio";
            if (originalFilename != null) {
                int dot = originalFilename.lastIndexOf('.');
                if (dot > 0 && dot < originalFilename.length() - 1) {
                    suffix = originalFilename.substring(dot);
                }
            }
            temp = Files.createTempFile("visit-audio-", suffix);
            Files.write(temp, bytes);
            AudioFile audioFile = AudioFileIO.read(temp.toFile());
            AudioHeader header = audioFile.getAudioHeader();
            if (header == null) {
                return 0;
            }
            return Math.max(header.getTrackLength(), 0);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            return 0;
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // temp cleanup best-effort
                }
            }
        }
    }
}
