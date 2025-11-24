package com.memozy.memozy_back.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /**
     * common. code prefix: common-
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "common-1", "서버 에러가 발생했습니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "common-2", "입력값이 올바르지 않습니다."),
    NOT_FOUND_RESOURCE_EXCEPTION(HttpStatus.NOT_FOUND, "common-3", "존재하지 않는 데이터입니다."),
    DUPLICATED_RESOURCE_EXCEPTION(HttpStatus.CONFLICT, "common-4", "이미 존재하는 데이터입니다."),
    PARSE_JSON_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "common-5", "JSON 파싱 에러가 발생했습니다."),
    UNSUPPORTED_MESSAGE_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "common-7", "지원하지 않는 채팅 메세지 타입입니다."),
    NOT_FOUND_HANDLER_EXCEPTION(HttpStatus.NOT_FOUND, "common-8", "지원하지 않는 Api 요청 입니다."),
    NOT_FOUND_USER_EXCEPTION(HttpStatus.NOT_FOUND, "common-9", "존재하지 않는 사용자입니다."),
    INVALID_ACCESS_EXCEPTION(HttpStatus.FORBIDDEN, "common-10", "잘못된 접근입니다."),
    BLOCKED_USER_EXCEPTION(HttpStatus.FORBIDDEN, "common-11", "활동제한이 걸린 사용자입니다."),

    /**
     * auth. code prefix: auth-
     */
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-1", "인증되지 않은 사용자입니다."),
    EXPIRED_ACCESS_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-2", "만료된 엑세스 토큰입니다."),
    EXPIRED_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-3", "만료된 리프레시 토큰입니다."),
    INVALID_ACCESS_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-4", "유효하지 않은 엑세스 토큰입니다."),
    INVALID_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-5", "유효하지 않은 리프레시 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-6", "지원하지 않는 JWT 토큰입니다."),
    UNSUPPORTED_SOCIAL_PLATFORM_EXCEPTION(HttpStatus.UNAUTHORIZED, "auth-7", "지원하지 않는 소셜 플랫폼입니다."),
    AUTH_MISSING_NAME(HttpStatus.BAD_REQUEST, "auth-8", "최초 로그인 시 name 값이 필요합니다."),
    AUTH_MISSING_EMAIL(HttpStatus.BAD_REQUEST, "auth-8", "최초 로그인 시 email 값이 필요합니다."),
    APPLE_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "auth-10", "Apple ID Token 검증에 실패했습니다."),
    APPLE_INVALID_ISSUER(HttpStatus.UNAUTHORIZED, "auth-11" , "애플 토큰의 발급자(issuer)가 유효하지 않습니다." ),

    /**
     * resource. code prefix: resource-
     */
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, "resource-1", "리소스에 접근 권한이 없습니다."),

    /**
     * s3. code prefix: s3-
     */
    NOT_FOUND_S3_RESOURCE_EXCEPTION(HttpStatus.NOT_FOUND, "s3-1", "S3 에서 해당 리소스를 찾을 수 없습니다."),

    /**
     * file. code prefix: file-
     */
    FILE_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "file-1", "파일 업로드에 실패했습니다."),
    IMAGE_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "file-3", "이미지 확장자 변환에 실패했습니다." ),

    /**
     * user-policy-agreement. code prefix: user-policy-agreement-
     */
    DUPLICATED_POLICY_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST, "user-policy-agreement-1",
            "같은 약관을 한 번에 여러 번 동의할 수 없습니다."),

    /**
     * friendship. code prefix: friend-
     */
    FORBIDDEN_FRIEND_ACCESS(HttpStatus.FORBIDDEN, "friend-1", "해당 유저와 친구 관계가 아닙니다."),
    SELF_FRIENDSHIP_EXCEPTION(HttpStatus.BAD_REQUEST, "friend-2", "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    DUPLICATE_RESOURCE_EXCEPTION(HttpStatus.BAD_REQUEST, "friend-3", "이미 친구 요청을 보냈습니다."),

    /**
     * flask, code prefix: flask-
     */
    NO_RESPONSE_FLASK_SERVER(HttpStatus.BAD_GATEWAY, "flask-1", "Flask 서버에서 유효한 응답을 받지 못했습니다."),

    /**
     * chat, code prefix: chat-
     */
    INVALID_MEMORY_ITEM_ID(HttpStatus.BAD_REQUEST, "chat-1", "잘못된 memoryItemId 요청입니다."),
    SSE_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "chat-2", "SSE 연결 중 오류가 발생했습니다."),
    SSE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "chat-3", "SSE 연결이 이미 종료되었습니다."),

    /**
     * memory, code prefix: memory-
     */
    MISSING_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "memory-1" , "검색 타입이 필요합니다." ),
    NOT_FOUND_TEMP_MEMORY(HttpStatus.NOT_FOUND, "memory-2", "작업 완료된 기록이 존재하지 않습니다."),
    CANNOT_MANAGE_OWNER_ACCESS(HttpStatus.BAD_REQUEST, "memory-3" , "소유자의 권한은 제어할 수 없습니다."),
    NOT_FOUND_PERMISSION(HttpStatus.BAD_REQUEST, "memory-5" , "해당 기록에 대한 접근 권한이 없습니다"),
    INVALID_PERMISSION_LEVEL(HttpStatus.BAD_REQUEST, "memory-6" , "유효하지 않은 권한 레벨입니다." ),
    NOT_FOUND_MEMORY(HttpStatus.NOT_FOUND, "memory-7" , "해당 기록을 찾을 수 없습니다." ),

    /**
     * lock, code prefix: lock
     */
    INVALID_LOCK_TOKEN_EXCEPTION(HttpStatus.FORBIDDEN, "lock-1" , "유효하지 않은 락 토큰입니다." ),
    LOCK_ALREADY_HELD(HttpStatus.CONFLICT, "lock-2", "이미 다른 사용자가 락을 소유중입니다." ),
    LOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "lock-3", "해당 락을 찾을 수 없습니다." ),
    LOCK_RELEASE_FAILED(HttpStatus.FORBIDDEN, "lock-4", "해당 락을 해제할 권한이 없습니다." ),

    /**
     * push-alert, code prefix: push-alert-
     */
    DEVICE_TOKEN_OWNED_BY_ANOTHER_USER(HttpStatus.CONFLICT, "push-alert-1", "해당 디바이스 토큰은 다른 사용자가 소유하고 있습니다."), ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}