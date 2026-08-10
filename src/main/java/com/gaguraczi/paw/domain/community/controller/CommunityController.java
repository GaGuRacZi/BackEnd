package com.gaguraczi.paw.domain.community.controller;

import com.gaguraczi.paw.domain.community.dto.req.CommunityCreateMultipart;
import com.gaguraczi.paw.domain.community.dto.req.CommunityCreateReq;
import com.gaguraczi.paw.domain.community.dto.req.CommunityUpdateMultipart;
import com.gaguraczi.paw.domain.community.dto.req.CommunityUpdateReq;
import com.gaguraczi.paw.domain.community.dto.res.CommunityDetailRes;
import com.gaguraczi.paw.domain.community.dto.res.CommunityListItemRes;
import com.gaguraczi.paw.domain.community.dto.res.CommunityTagRes;
import com.gaguraczi.paw.domain.community.dto.res.LikeToggleRes;
import com.gaguraczi.paw.domain.community.enums.CommunitySort;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.exception.code.CommunitySuccessCode;
import com.gaguraczi.paw.domain.community.service.CommunityCommandService;
import com.gaguraczi.paw.domain.community.service.CommunityService;
import com.gaguraczi.paw.domain.like.service.CommunityLikeService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "communities", description = "커뮤니티 API (소통/장터). JWT Bearer 필수. REVIEW 타입은 피드/작성 미지원.")
@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final CommunityCommandService communityCommandService;
    private final CommunityLikeService communityLikeService;

    @Operation(
            summary = "커뮤니티 태그(칩) 목록",
            description = """
                    postType별 태그(칩) 목록을 반환합니다.
                    - 글쓰기/수정/목록 필터에는 `tagCode`(CommunityTagCode enum)를 사용하세요. tagId는 사용하지 않습니다.
                    - COMMUNICATION / MARKET 만 피드·작성에 사용
                    - JWT Bearer 필수
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMUNITY_TAG_LIST_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "소통 태그 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMUNITY_TAG_LIST_200",
                                              "message": "커뮤니티 태그 목록 조회에 성공했습니다.",
                                              "result": [
                                                {
                                                  "tagName": "건강상담",
                                                  "tagCode": "HEALTH_CONSULT",
                                                  "postType": "COMMUNICATION",
                                                  "sortOrder": 1
                                                },
                                                {
                                                  "tagName": "산책친구",
                                                  "tagCode": "WALK_BUDDY",
                                                  "postType": "COMMUNICATION",
                                                  "sortOrder": 2
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (COMMUNITY_400_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "미지원 postType",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_400_1",
                                              "message": "지원하지 않는 게시글 유형입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/community-tags")
    public ApiResponse<List<CommunityTagRes>> listTags(
            @Parameter(description = "게시글 유형", required = true, example = "COMMUNICATION")
            @RequestParam PostType postType
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_TAG_LIST_200,
                communityService.listTags(postType)
        );
    }

    @Operation(
            summary = "커뮤니티 목록 (커서 슬라이딩)",
            description = """
                    커서 기반 게시글 목록입니다.
                    - sort: LATEST(기본) | LIKE | VIEW | COMMENT
                    - tagCode: CommunityTagCode enum 필터 (예: HEALTH_CONSULT, FOOD_SNACK)
                    - size 기본 20, 최대 50
                    - cursor는 이전 응답의 nextCursor를 그대로 전달 (sort 변경 시 기존 cursor 무효)
                    - COMMUNICATION이면 status/tradeType 무시
                    - viewCount/likeCount는 Redis 캐시 우선
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMUNITY_LIST_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "장터 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMUNITY_LIST_200",
                                              "message": "커뮤니티 목록 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "postId": 10,
                                                    "postType": "MARKET",
                                                    "tagName": "사료·간식",
                                                    "tagCode": "FOOD_SNACK",
                                                    "title": "사료 나눔합니다",
                                                    "contentPreview": "개봉만 했습니다. 주말에 직거래 가능해요.",
                                                    "viewCount": 12,
                                                    "likeCount": 3,
                                                    "commentCount": 1,
                                                    "authorNickname": "길동이",
                                                    "thumbnailUrl": "https://cdn.example.com/community/10/a.jpg",
                                                    "tradeType": "SHARE",
                                                    "marketStatus": "IN_PROGRESS",
                                                    "price": null,
                                                    "priceNegotiable": false,
                                                    "regionName": "서울특별시 강남구",
                                                    "createdAt": "2026-08-08T10:00:00"
                                                  }
                                                ],
                                                "nextCursor": "eyJzIjoiTEFURVNUIiwidCI6IjIwMjYtMDgtMDhUMTA6MDA6MDAiLCJpZCI6MTB9",
                                                "hasNext": true,
                                                "size": 20
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (COMMUNITY_400_1 / COMMUNITY_400_2)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "미지원 postType",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_1",
                                                      "message": "지원하지 않는 게시글 유형입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "유효하지 않은 커서",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_2",
                                                      "message": "유효하지 않은 커서입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/communities")
    public ApiResponse<CursorPageRes<CommunityListItemRes>> list(
            @Parameter(description = "게시글 유형 (COMMUNICATION | MARKET)", required = true, example = "MARKET")
            @RequestParam PostType postType,
            @Parameter(description = "태그 필터 (CommunityTagCode)", example = "FOOD_SNACK")
            @RequestParam(required = false) CommunityTagCode tagCode,
            @Parameter(description = "장터 상태 필터 (MARKET만)", example = "IN_PROGRESS")
            @RequestParam(required = false) MarketStatus status,
            @Parameter(description = "장터 거래유형 필터 (MARKET만)", example = "SHARE")
            @RequestParam(required = false) MarketTradeType tradeType,
            @Parameter(description = "정렬", example = "LATEST")
            @RequestParam(required = false, defaultValue = "LATEST") CommunitySort sort,
            @Parameter(description = "이전 응답의 nextCursor", example = "eyJzIjoiTEFURVNUIiwidCI6IjIwMjYtMDgtMDhUMTA6MDA6MDAiLCJpZCI6MTB9")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_LIST_200,
                communityService.list(postType, tagCode, status, tradeType, sort, cursor, size)
        );
    }

    @Operation(
            summary = "커뮤니티 상세",
            description = """
                    게시글 상세를 조회하고 Redis 조회수를 증가시킵니다.
                    - likedByMe: 현재 로그인 유저 기준
                    - JWT Bearer 필수
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMUNITY_DETAIL_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "소통 상세",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMUNITY_DETAIL_200",
                                              "message": "커뮤니티 상세 조회에 성공했습니다.",
                                              "result": {
                                                "postId": 10,
                                                "postType": "COMMUNICATION",
                                                "tagName": "건강상담",
                                                "tagCode": "HEALTH_CONSULT",
                                                "title": "산책 후 발바닥이 빨개요",
                                                "content": "병원 가봐야 할까요?",
                                                "hashTags": ["산책", "건강"],
                                                "photos": [
                                                  {
                                                    "photoId": 1,
                                                    "url": "https://cdn.example.com/community/10/a.jpg",
                                                    "isThumbnail": true,
                                                    "sortOrder": 0
                                                  }
                                                ],
                                                "viewCount": 13,
                                                "likeCount": 3,
                                                "commentCount": 2,
                                                "likedByMe": false,
                                                "authorNickname": "길동이",
                                                "tradeType": null,
                                                "marketStatus": null,
                                                "price": null,
                                                "priceNegotiable": false,
                                                "expiryDate": null,
                                                "tradeMethod": null,
                                                "regionName": null,
                                                "createdAt": "2026-08-08T10:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (COMMUNITY_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "게시글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404",
                                              "message": "게시글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/communities/{postId}")
    public ApiResponse<CommunityDetailRes> detail(
            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long postId
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_DETAIL_200,
                communityService.getDetail(postId)
        );
    }

    @Operation(
            summary = "커뮤니티 게시글 작성",
            description = """
                    multipart/form-data: data(JSON) + images(0~5장)
                    - 이미지: 파일당 최대 5MB, JPEG/PNG/GIF/WEBP/HEIC/HEIF
                    - MARKET이면 tradeType·tradeMethod 필수, marketStatus는 서버가 IN_PROGRESS로 설정
                    - JWT Bearer 필수
                    
                    data JSON 예시 (소통):
                    {"postType":"COMMUNICATION","tagCode":"HEALTH_CONSULT","title":"산책 친구 구해요","content":"주말에 같이 산책하실 분","hashTags":["산책"],"thumbnailIndex":0}
                    
                    data JSON 예시 (장터):
                    {"postType":"MARKET","tagCode":"FOOD_SNACK","title":"사료 나눔","content":"개봉만 했습니다","hashTags":["나눔","사료"],"tradeType":"SHARE","tradeMethod":"DIRECT","price":null,"priceNegotiable":false,"expiryDate":"2026-09-01","regionCode":"1168010100","thumbnailIndex":0}
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CommunityCreateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "images", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMUNITY_CREATE_200) — result는 CommunityDetailRes",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "작성 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMUNITY_CREATE_200",
                                              "message": "게시글이 등록되었습니다.",
                                              "result": {
                                                "postId": 11,
                                                "postType": "MARKET",
                                                "tagName": "사료·간식",
                                                "tagCode": "FOOD_SNACK",
                                                "title": "사료 나눔",
                                                "content": "개봉만 했습니다",
                                                "hashTags": ["나눔", "사료"],
                                                "photos": [],
                                                "viewCount": 0,
                                                "likeCount": 0,
                                                "commentCount": 0,
                                                "likedByMe": false,
                                                "authorNickname": "길동이",
                                                "tradeType": "SHARE",
                                                "marketStatus": "IN_PROGRESS",
                                                "price": null,
                                                "priceNegotiable": false,
                                                "expiryDate": "2026-09-01",
                                                "tradeMethod": "DIRECT",
                                                "regionName": "서울특별시 강남구",
                                                "createdAt": "2026-08-08T12:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증/비즈니스 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "장터 필수값 누락",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_6",
                                                      "message": "장터 게시글에 필수 값이 누락되었습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "사진 개수 초과",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_7",
                                                      "message": "사진은 최대 5장까지 업로드할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미지 형식 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_10",
                                                      "message": "지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "태그 불일치",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_5",
                                                      "message": "게시글 유형과 태그가 일치하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "태그/지역 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "태그 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_404_2",
                                                      "message": "커뮤니티 태그를 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "지역 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_404_4",
                                                      "message": "지역을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping(value = "/communities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunityDetailRes> create(
            @RequestPart("data") @Valid CommunityCreateReq data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_CREATE_200,
                communityCommandService.create(data, images)
        );
    }

    @Operation(
            summary = "커뮤니티 게시글 수정",
            description = """
                    multipart/form-data: data(JSON) + images(신규, 선택)
                    - 작성자만 가능
                    - keepPhotoUrls에 없는 기존 사진은 삭제
                    - 최종 사진 수 = keepPhotoUrls + 신규 images ≤ 5
                    - postType은 수정 불가
                    
                    data JSON 예시:
                    {"tagCode":"FOOD_SNACK","title":"사료 나눔 (예약중)","content":"개봉만 했습니다. 내일까지 가능해요.","hashTags":["나눔"],"keepPhotoUrls":["https://cdn.example.com/community/10/a.jpg"],"thumbnailUrl":"https://cdn.example.com/community/10/a.jpg","tradeType":"SHARE","marketStatus":"RESERVED","price":null,"priceNegotiable":false,"expiryDate":"2026-09-01","tradeMethod":"DIRECT","regionCode":"1168010100"}
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CommunityUpdateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "images", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMUNITY_UPDATE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "수정 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMUNITY_UPDATE_200",
                                              "message": "게시글이 수정되었습니다.",
                                              "result": {
                                                "postId": 10,
                                                "postType": "MARKET",
                                                "tagName": "사료·간식",
                                                "tagCode": "FOOD_SNACK",
                                                "title": "사료 나눔 (예약중)",
                                                "content": "개봉만 했습니다. 내일까지 가능해요.",
                                                "hashTags": ["나눔"],
                                                "photos": [
                                                  {
                                                    "photoId": 1,
                                                    "url": "https://cdn.example.com/community/10/a.jpg",
                                                    "isThumbnail": true,
                                                    "sortOrder": 0
                                                  }
                                                ],
                                                "viewCount": 13,
                                                "likeCount": 3,
                                                "commentCount": 1,
                                                "likedByMe": false,
                                                "authorNickname": "길동이",
                                                "tradeType": "SHARE",
                                                "marketStatus": "RESERVED",
                                                "price": null,
                                                "priceNegotiable": false,
                                                "expiryDate": "2026-09-01",
                                                "tradeMethod": "DIRECT",
                                                "regionName": "서울특별시 강남구",
                                                "createdAt": "2026-08-08T10:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "작성자 아님 (COMMUNITY_403)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_403",
                                              "message": "권한이 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (COMMUNITY_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "게시글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404",
                                              "message": "게시글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 오류 (사진/썸네일 등)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "사진 개수 초과",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_7",
                                                      "message": "사진은 최대 5장까지 업로드할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "썸네일 무효",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_11",
                                                      "message": "썸네일로 지정할 사진을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping(value = "/communities/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunityDetailRes> update(
            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long postId,
            @RequestPart("data") @Valid CommunityUpdateReq data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_UPDATE_200,
                communityCommandService.update(postId, data, images)
        );
    }

    @Operation(
            summary = "커뮤니티 게시글 삭제",
            description = "작성자만 삭제 가능. S3 사진·Redis 카운트도 정리됩니다. JWT Bearer 필수."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMUNITY_DELETE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "삭제 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMUNITY_DELETE_200",
                                              "message": "게시글이 삭제되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "작성자 아님 (COMMUNITY_403)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_403",
                                              "message": "권한이 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (COMMUNITY_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "게시글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404",
                                              "message": "게시글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/communities/{postId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long postId
    ) {
        communityCommandService.delete(postId);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_DELETE_200, null);
    }

    @Operation(
            summary = "게시글 좋아요 토글",
            description = "좋아요가 없으면 등록, 있으면 취소합니다. Body 없음. likeCount는 커밋 전 카운트 기반의 낙관적 추정값이며 Redis 절대 카운트가 아닙니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (LIKE_TOGGLE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "좋아요 등록",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LIKE_TOGGLE_200",
                                                      "message": "좋아요 상태가 변경되었습니다.",
                                                      "result": {
                                                        "liked": true,
                                                        "likeCount": 4
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "좋아요 취소",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LIKE_TOGGLE_200",
                                                      "message": "좋아요 상태가 변경되었습니다.",
                                                      "result": {
                                                        "liked": false,
                                                        "likeCount": 3
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (COMMUNITY_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "게시글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404",
                                              "message": "게시글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/communities/{postId}/likes")
    public ApiResponse<LikeToggleRes> toggleLike(
            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long postId
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.LIKE_TOGGLE_200,
                communityLikeService.toggle(postId)
        );
    }
}
