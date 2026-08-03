package com.gaguraczi.paw.domain.terms.controller;

import com.gaguraczi.paw.domain.terms.dto.res.TermsDetailRes;
import com.gaguraczi.paw.domain.terms.dto.res.TermsSummaryRes;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import com.gaguraczi.paw.domain.terms.exception.code.TermsSuccessCode;
import com.gaguraczi.paw.domain.terms.service.TermsService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "terms", description = "약관 API")
@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "약관 목록")
    @GetMapping
    public ApiResponse<List<TermsSummaryRes>> list() {
        return ApiResponse.onSuccess(TermsSuccessCode.TERMS_LIST_200, termsService.list());
    }

    @Operation(summary = "약관 상세")
    @GetMapping("/{type}")
    public ApiResponse<TermsDetailRes> detail(@PathVariable TermsType type) {
        return ApiResponse.onSuccess(TermsSuccessCode.TERMS_DETAIL_200, termsService.detail(type));
    }
}
