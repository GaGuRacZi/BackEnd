package com.gaguraczi.paw.domain.region.service;

import com.gaguraczi.paw.domain.region.dto.res.RegionSearchRes;
import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.region.enums.RegionLevel;
import com.gaguraczi.paw.domain.region.exception.code.RegionErrorCode;
import com.gaguraczi.paw.domain.region.repository.LegalRegionRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LegalRegionService {

    private static final int DONG_PREVIEW_LIMIT = 5;

    private final LegalRegionRepository legalRegionRepository;

    public List<RegionSearchRes> search(String q) {
        if (q == null || q.isBlank()) {
            throw GeneralException.of(RegionErrorCode.REGION_QUERY_REQUIRED);
        }

        String keyword = q.trim();
        List<LegalRegion> regions = legalRegionRepository.searchActiveSigungu(keyword);

        return regions.stream()
                .limit(50)
                .map(this::toSearchRes)
                .toList();
    }

    public LegalRegion requireActiveSigungu(String regionCode) {
        LegalRegion region = legalRegionRepository.findById(regionCode)
                .orElseThrow(() -> GeneralException.of(RegionErrorCode.REGION_NOT_FOUND));

        if (region.getLevel() != RegionLevel.SIGUNGU) {
            throw GeneralException.of(RegionErrorCode.REGION_LEVEL_INVALID);
        }
        if (region.isAbolished()) {
            throw GeneralException.of(RegionErrorCode.REGION_ABOLISHED);
        }
        return region;
    }

    /**
     * 법정동 10자리 코드에서 시군구(SIGUNGU) LegalRegion을 찾습니다.
     * 예: 1111010100 → 1111000000, 없으면 상위 1110000000 폴백.
     */
    public LegalRegion requireActiveSigunguByLegalDistrictCode(String legalDistrictCode) {
        if (legalDistrictCode == null || legalDistrictCode.length() < 5) {
            throw GeneralException.of(RegionErrorCode.REGION_NOT_FOUND);
        }
        String sigunguCode = legalDistrictCode.substring(0, 5) + "00000";
        return legalRegionRepository.findByCodeAndAbolishedFalseAndLevel(sigunguCode, RegionLevel.SIGUNGU)
                .or(() -> {
                    if (legalDistrictCode.length() < 4) {
                        return java.util.Optional.empty();
                    }
                    String parentCode = legalDistrictCode.substring(0, 4) + "000000";
                    if (parentCode.equals(sigunguCode)) {
                        return java.util.Optional.empty();
                    }
                    return legalRegionRepository.findByCodeAndAbolishedFalseAndLevel(parentCode, RegionLevel.SIGUNGU);
                })
                .orElseThrow(() -> GeneralException.of(RegionErrorCode.REGION_NOT_FOUND));
    }

    private RegionSearchRes toSearchRes(LegalRegion region) {
        List<LegalRegion> dongs = legalRegionRepository
                .findTop20ByParentCodeAndLevelAndAbolishedFalseOrderByNameAsc(
                        region.getCode(), RegionLevel.DONG);

        List<String> preview = dongs.stream()
                .limit(DONG_PREVIEW_LIMIT)
                .map(d -> shortDongName(d.getName(), region.getName()))
                .toList();

        return RegionSearchRes.builder()
                .code(region.getCode())
                .name(region.getName())
                .dongPreview(preview)
                .build();
    }

    private String shortDongName(String fullName, String parentName) {
        String prefix = parentName + " ";
        if (fullName.startsWith(prefix)) {
            return fullName.substring(prefix.length());
        }
        int idx = fullName.lastIndexOf(' ');
        return idx >= 0 ? fullName.substring(idx + 1) : fullName;
    }
}
