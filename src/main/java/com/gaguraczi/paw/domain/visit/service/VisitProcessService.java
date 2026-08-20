package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.visit.client.DiarizedTranscript;
import com.gaguraczi.paw.domain.visit.client.OpenAiSttClient;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.fcm.VisitFcmService;
import com.gaguraczi.paw.global.config.AsyncConfig;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitProcessService {

    private final VisitProcessTxService visitProcessTxService;
    private final S3Utils s3Utils;
    private final OpenAiSttClient openAiSttClient;
    private final VisitSpeakerMapper visitSpeakerMapper;
    private final VisitShortSummaryService visitShortSummaryService;
    private final VisitProperties visitProperties;
    private final VisitFcmService visitFcmService;

    @Async(AsyncConfig.VISIT_TASK_EXECUTOR)
    public void processAsync(Long visitId) {
        try {
            process(visitId);
        } catch (Exception e) {
            log.error("Visit processing failed visitId={}", visitId, e);
            String message = e instanceof GeneralException ge ? ge.getMessage() : "진료 요약 생성에 실패했습니다.";
            visitProcessTxService.markFailed(visitId, message);
            try {
                notifyStatus(visitId, VisitFcmService.TYPE_FAILED);
            } catch (Exception notifyEx) {
                log.warn("Visit failed push failed visitId={}: {}", visitId, notifyEx.getMessage());
            }
        }
    }

    public void handleSubmitRejected(Long visitId) {
        log.error("Visit processing rejected visitId={}", visitId);
        visitProcessTxService.markFailed(visitId, "진료 요약 생성에 실패했습니다.");
        try {
            notifyStatus(visitId, VisitFcmService.TYPE_FAILED);
        } catch (Exception e) {
            log.warn("Visit failed push failed visitId={}: {}", visitId, e.getMessage());
        }
    }

    void process(Long visitId) {
        Visit visit = visitProcessTxService.requireForProcessing(visitId);
        Path audio = null;
        try {
            audio = s3Utils.downloadToTempFile(visit.getAudioS3Key());
            DiarizedTranscript transcript = openAiSttClient.transcribe(
                    audio,
                    filenameOf(visit.getAudioS3Key()),
                    visit.getAudioContentType()
            );
            if (transcript.durationSec() != null && transcript.durationSec() > visitProperties.getMaxAudioDurationSec()) {
                throw GeneralException.of(VisitErrorCode.VISIT_AUDIO_DURATION);
            }
            Pet pet = visit.getPet();
            List<VisitSpeakerMapper.MappedTurn> turns = visitSpeakerMapper.map(transcript, pet.getPetName());
            VisitShortSummary summary = visitShortSummaryService.summarize(turns, pet);
            visitProcessTxService.saveReady(visitId, turns, summary, transcript.durationSec());
            try {
                notifyStatus(visitId, VisitFcmService.TYPE_READY);
            } catch (Exception e) {
                log.warn("Visit ready push failed visitId={}: {}", visitId, e.getMessage());
            }
        } finally {
            if (audio != null) {
                try {
                    Files.deleteIfExists(audio);
                } catch (Exception e) {
                    log.warn("Failed to delete visit audio temp file visitId={}: {}", visitId, e.getMessage());
                }
            }
        }
    }

    private void notifyStatus(Long visitId, String type) {
        visitProcessTxService.loadNotifyTarget(visitId).ifPresent(target ->
                visitFcmService.notifyStatus(target.user(), type, target.visitId(), target.petId())
        );
    }

    private static String filenameOf(String key) {
        if (key == null || key.isBlank()) {
            return "visit-audio.m4a";
        }
        int slash = key.lastIndexOf('/');
        return slash >= 0 ? key.substring(slash + 1) : key;
    }
}
