package com.gaguraczi.paw.domain.visit.support;

import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisitAudioValidatorTest {

    private VisitAudioValidator validator;

    @BeforeEach
    void setUp() {
        validator = new VisitAudioValidator(new VisitProperties());
    }

    @Test
    void rejectsWavExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "audio", "clinic.wav", "audio/wav", wavBytes());

        assertThatThrownBy(() -> validator.validateAndDurationSec(file))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AUDIO_TYPE);
    }

    @Test
    void rejectsWaveExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "audio", "clinic.wave", "audio/wave", wavBytes());

        assertThatThrownBy(() -> validator.validateAndDurationSec(file))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AUDIO_TYPE);
    }

    @Test
    void rejectsWavMimeEvenIfExtensionIsM4a() {
        MockMultipartFile file = new MockMultipartFile(
                "audio", "clinic.m4a", "audio/mp4", wavBytes());

        assertThatThrownBy(() -> validator.validateAndDurationSec(file))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AUDIO_TYPE);
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("audio", "clinic.mp3", "audio/mpeg", new byte[0]);

        assertThatThrownBy(() -> validator.validateAndDurationSec(file))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AUDIO_REQUIRED);
    }

    @Test
    void rejectsTooLargeFile() {
        VisitProperties properties = new VisitProperties();
        properties.setMaxAudioBytes(4);
        validator = new VisitAudioValidator(properties);
        MockMultipartFile file = new MockMultipartFile(
                "audio", "clinic.mp3", "audio/mpeg", new byte[]{1, 2, 3, 4, 5});

        assertThatThrownBy(() -> validator.validateAndDurationSec(file))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AUDIO_TOO_LARGE);
    }

    @Test
    void rejectsWhenDurationHeaderCannotBeRead() {
        byte[] id3 = new byte[128];
        byte[] tag = "ID3".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(tag, 0, id3, 0, 3);
        MockMultipartFile file = new MockMultipartFile(
                "audio", "clinic.mp3", "audio/mpeg", id3);

        assertThatThrownBy(() -> validator.validateAndDurationSec(file))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AUDIO_TYPE);
    }

    private static byte[] wavBytes() {
        byte[] wav = new byte[44];
        byte[] riff = "RIFF".getBytes(StandardCharsets.US_ASCII);
        byte[] wave = "WAVE".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(riff, 0, wav, 0, 4);
        System.arraycopy(wave, 0, wav, 8, 4);
        return wav;
    }
}
