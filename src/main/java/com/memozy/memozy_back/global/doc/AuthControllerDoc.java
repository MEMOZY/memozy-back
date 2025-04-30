//package com.memozy.memozy_back.global.doc;
//
//
//
//import static com.memozy.memozy_back.global.doc.discription.ResponseCodeDescription.BAD_REQUEST_DESCRIPTION;
//import static com.memozy.memozy_back.global.doc.discription.ResponseCodeDescription.BAD_REQUEST_DESCRIPTION1;
//import static com.memozy.memozy_back.global.doc.discription.ResponseCodeDescription.BAD_REQUEST_DESCRIPTION2;
//import static com.memozy.memozy_back.global.doc.discription.ResponseCodeDescription.NOT_FOUND_DESCRIPTION;
//import static com.memozy.memozy_back.global.doc.discription.ResponseCodeDescription.OK_DESCRIPTION;
//import static com.memozy.memozy_back.global.doc.discription.ResponseCodeDescription.UNAUTHORIZED_DESCRIPTION;
//import static com.memozy.memozy_back.global.doc.response.ErrorResponseDoc.BAD_REQUEST_RESPONSE1;
//import static com.memozy.memozy_back.global.doc.response.ErrorResponseDoc.BAD_REQUEST_RESPONSE2;
//import static com.memozy.memozy_back.global.doc.response.ErrorResponseDoc.NOT_FOUND_RESPONSE;
//import static com.memozy.memozy_back.global.doc.response.ErrorResponseDoc.UNAUTHORIZED_RESPONSE;
//
//import com.memozy.memozy_back.domain.auth.dto.request.LoginRequest;
//import com.memozy.memozy_back.domain.auth.dto.request.ReissueTokenRequest;
//import com.memozy.memozy_back.domain.auth.dto.request.SocialLoginRequest;
//import com.memozy.memozy_back.domain.auth.dto.response.LoginResponse;
//import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.enums.ParameterIn;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.ExampleObject;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.ResponseEntity;
//
//@Tag(name = "Auth", description = "회원 인증 API")
//public interface AuthControllerDoc {
//
//    @Operation(summary = "이메일/비밀번호 로그인")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = OK_DESCRIPTION,
//                    useReturnTypeSchema = true),
//            @ApiResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
//                    content = @Content(examples = {
//                            @ExampleObject(name = "Example1: One Field Error", description = BAD_REQUEST_DESCRIPTION1,
//                                    value = BAD_REQUEST_RESPONSE1),
//                            @ExampleObject(name = "Example2: Multiple Field Error", description = BAD_REQUEST_DESCRIPTION2,
//                                    value = BAD_REQUEST_RESPONSE2)})),
//            @ApiResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
//                    content = @Content(examples = @ExampleObject(value = NOT_FOUND_RESPONSE)))
//    })
//    ResponseEntity<LoginResponse> login(LoginRequest request);
//
//    @Operation(summary = "소셜 로그인")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = OK_DESCRIPTION,
//                    useReturnTypeSchema = true),
//            @ApiResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
//                    content = @Content(examples = {
//                            @ExampleObject(name = "Example1: One Field Error", description = BAD_REQUEST_DESCRIPTION1,
//                                    value = BAD_REQUEST_RESPONSE1),
//                            @ExampleObject(name = "Example2: Multiple Field Error", description = BAD_REQUEST_DESCRIPTION2,
//                                    value = BAD_REQUEST_RESPONSE2)}))
//    })
//    @Parameter(name = "provider", description = "소셜 로그인 타입(kakako, google)", in = ParameterIn.PATH)
//    @Parameter(name = "code", description = "인가 코드", in = ParameterIn.QUERY)
//    ResponseEntity<LoginResponse> socialLogin(String origin, SocialPlatform socialPlatform, SocialLoginRequest request);
//
////    @Operation(summary = "Access Token 검증", description = "Access Token을 통해 현재 로그인 된 사용자의 정보 요청")
////    @ApiResponses({
////            @ApiResponse(responseCode = "200", description = OK_DESCRIPTION,
////                    useReturnTypeSchema = true),
////            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
////                    content = @Content(examples = @ExampleObject(value = UNAUTHORIZED_RESPONSE))),
////            @ApiResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
////                    content = @Content(examples = @ExampleObject(value = NOT_FOUND_RESPONSE)))
////    })
////    ResponseEntity<UserSummary> validateToken(@Parameter(hidden = true) Long loginId);
//
//    @Operation(summary = "Access / Refresh Token 재발급", description = "Refresh Token을 통해 토큰 재발급")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = OK_DESCRIPTION,
//                    useReturnTypeSchema = true),
//            @ApiResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
//                    content = @Content(examples = @ExampleObject(value = UNAUTHORIZED_RESPONSE))),
//            @ApiResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
//                    content = @Content(examples = @ExampleObject(value = NOT_FOUND_RESPONSE)))
//    })
//    ResponseEntity<LoginResponse> reissue(ReissueTokenRequest request);
//}