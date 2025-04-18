package com.memozy.memozy_back.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            log.error("error : {}", e.getMessage(), e);
            e.printStackTrace();
            responseError(
                    response,
                    e.getErrorCode(),
                    e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("error : {}", e.getMessage(), e);
            responseError(
                    response,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private void responseError(
            HttpServletResponse response,
            ErrorCode errorCode,
            String message)
            throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write(objectMapper.writeValueAsString(
                        new ErrorResponse(
                                errorCode.getCode(),
                                message)));
    }
}