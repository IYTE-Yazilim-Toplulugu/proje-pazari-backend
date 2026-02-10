package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.presentation.mappers.IRequestMapper;
import com.iyte_yazilim.proje_pazari.presentation.security.UserPrincipal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public abstract class BaseController {

    @Autowired protected IMediator mediator;

    @Autowired protected IRequestMapper requestMapper;

    protected <T> ResponseEntity<ApiResponse<T>> send(IRequest<ApiResponse<T>> request) {
        ApiResponse<T> response = mediator.send(request);
        HttpStatus status = resolveHttpStatus(response.getCode());
        return ResponseEntity.status(status).body(response);
    }

    protected HttpStatus resolveHttpStatus(ResponseCode code) {
        return switch (code) {
            case SUCCESS -> HttpStatus.OK;
            case CREATED -> HttpStatus.CREATED;
            case ACCEPTED -> HttpStatus.OK;
            case NO_CONTENT -> HttpStatus.NO_CONTENT;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    protected UserPrincipal getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    protected String getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getUserId();
    }

    protected String getCurrentUserEmail(Authentication authentication) {
        return getCurrentUser(authentication).getEmail();
    }

    protected String getCurrentUserRole(Authentication authentication) {
        return getCurrentUser(authentication).getRole();
    }

    @SuppressWarnings("unchecked")
    protected <T> ResponseEntity<ApiResponse<T>> send(
            Class<? extends IRequest<ApiResponse<T>>> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth) {
        IRequest<ApiResponse<T>> request =
                (IRequest<ApiResponse<T>>)
                        requestMapper.map(requestClass, pathVariables, queryParams, body, auth);
        return send(request);
    }

    @SuppressWarnings("unchecked")
    protected <T> ResponseEntity<ApiResponse<T>> send(
            Class<? extends IRequest<ApiResponse<T>>> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth,
            Map<String, Object> extraFields) {
        IRequest<ApiResponse<T>> request =
                (IRequest<ApiResponse<T>>)
                        requestMapper.map(
                                requestClass, pathVariables, queryParams, body, auth, extraFields);
        return send(request);
    }
}
