package com.gaguraczi.paw.domain.users.controller;

/**
 * Swagger annotation constants for admin user APIs. Documentation only.
 */
public final class AdminUserApiDocs {

    private AdminUserApiDocs() {
    }

    public static final String TAG_DESCRIPTION = """
            관리자 계정 API입니다. ADMIN 역할 JWT 필수.
            
            하드탈퇴는 대상 유저와 연관 DB 행을 물리 삭제합니다. 본인·ADMIN 계정은 삭제할 수 없습니다.
            """;

    public static final String HARD_DELETE_DESCRIPTION = """
            ADMIN JWT 필수. 대상 유저와 연관 DB 행을 물리 삭제합니다.
            
            - 본인 계정은 `USER_400_4`, ADMIN 계정은 `USER_400_5`.
            - 없는 uid는 `USER_404`.
            - 소프트 탈퇴된 계정도 대상입니다.
            - 해당 유저의 커뮤니티 글/댓글/채팅방도 삭제됩니다.
            """;

    public static final String HARD_DELETE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "USER_HARD_DELETE_200",
              "message": "계정이 삭제되었습니다.",
              "result": null
            }
            """;

    public static final String USER_400_4_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "USER_400_4",
              "message": "본인 계정은 하드탈퇴할 수 없습니다.",
              "result": null
            }
            """;

    public static final String USER_400_5_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "USER_400_5",
              "message": "관리자 계정은 하드탈퇴할 수 없습니다.",
              "result": null
            }
            """;

    public static final String USER_404_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "USER_404",
              "message": "사용자를 찾을 수 없습니다.",
              "result": null
            }
            """;
}
