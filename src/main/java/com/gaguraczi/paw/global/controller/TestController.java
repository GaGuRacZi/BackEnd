package com.gaguraczi.paw.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.code.GeneralSuccessCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "health", description = "health check 관련 api 입니다.")
@RestController("/test")
public class TestController {
    @Operation(summary = "test용입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON_200", description = "Success"),
    })
    @GetMapping
    public ApiResponse<?> test() {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,null);//null자리에 dto객체를 넣어서 사용할 것
    }
}

