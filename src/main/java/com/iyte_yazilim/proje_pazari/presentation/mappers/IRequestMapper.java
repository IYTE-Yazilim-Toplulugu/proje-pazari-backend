package com.iyte_yazilim.proje_pazari.presentation.mappers;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import java.util.Map;
import org.springframework.security.core.Authentication;

public interface IRequestMapper {

    <T extends IRequest<?>> T map(
            Class<T> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth);

    <T extends IRequest<?>> T map(
            Class<T> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth,
            Map<String, Object> extraFields);
}
