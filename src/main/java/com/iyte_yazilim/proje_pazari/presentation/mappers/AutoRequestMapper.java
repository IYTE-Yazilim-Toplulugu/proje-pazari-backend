package com.iyte_yazilim.proje_pazari.presentation.mappers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.security.UserPrincipal;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AutoRequestMapper implements IRequestMapper {

    private static final Set<String> AUTH_USER_FIELDS =
            Set.of("userId", "ownerId", "authenticatedUserId", "requesterId", "operatorId");

    private final ObjectMapper objectMapper;

    public AutoRequestMapper() {
        this.objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public <T extends IRequest<?>> T map(
            Class<T> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth) {
        return map(requestClass, pathVariables, queryParams, body, auth, null);
    }

    @Override
    public <T extends IRequest<?>> T map(
            Class<T> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth,
            Map<String, Object> extraFields) {

        if (hasMultipartField(requestClass)) {
            return buildViaConstructor(
                    requestClass, pathVariables, queryParams, body, auth, extraFields);
        }

        Map<String, Object> dataMap = new HashMap<>();

        if (body != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.convertValue(body, Map.class);
            dataMap.putAll(bodyMap);
        }

        if (queryParams != null) {
            dataMap.putAll(queryParams);
        }

        if (pathVariables != null) {
            dataMap.putAll(pathVariables);
        }

        injectAuthFields(requestClass, dataMap, auth);

        if (extraFields != null) {
            dataMap.putAll(extraFields);
        }

        return objectMapper.convertValue(dataMap, requestClass);
    }

    private <T> boolean hasMultipartField(Class<T> requestClass) {
        if (!requestClass.isRecord()) {
            return false;
        }
        for (RecordComponent rc : requestClass.getRecordComponents()) {
            if (MultipartFile.class.isAssignableFrom(rc.getType())) {
                return true;
            }
        }
        return false;
    }

    private <T extends IRequest<?>> T buildViaConstructor(
            Class<T> requestClass,
            Map<String, String> pathVariables,
            Map<String, String> queryParams,
            Object body,
            Authentication auth,
            Map<String, Object> extraFields) {

        Map<String, Object> dataMap = new HashMap<>();

        if (body != null && !(body instanceof MultipartFile)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.convertValue(body, Map.class);
            dataMap.putAll(bodyMap);
        }

        if (queryParams != null) {
            dataMap.putAll(queryParams);
        }

        if (pathVariables != null) {
            dataMap.putAll(pathVariables);
        }

        injectAuthFields(requestClass, dataMap, auth);

        if (extraFields != null) {
            dataMap.putAll(extraFields);
        }

        RecordComponent[] components = requestClass.getRecordComponents();
        Class<?>[] paramTypes = new Class<?>[components.length];
        Object[] args = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
            String name = components[i].getName();
            Object value = dataMap.get(name);

            if (value != null && !paramTypes[i].isAssignableFrom(value.getClass())) {
                args[i] = objectMapper.convertValue(value, paramTypes[i]);
            } else {
                args[i] = value;
            }
        }

        try {
            Constructor<T> ctor = requestClass.getDeclaredConstructor(paramTypes);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to construct "
                            + requestClass.getSimpleName()
                            + " via canonical constructor",
                    e);
        }
    }

    private void injectAuthFields(
            Class<?> requestClass, Map<String, Object> dataMap, Authentication auth) {
        if (auth == null) {
            return;
        }

        String currentUserId = getUserIdFromAuth(auth);
        if (currentUserId == null) {
            return;
        }

        if (!requestClass.isRecord()) {
            return;
        }

        for (RecordComponent rc : requestClass.getRecordComponents()) {
            if (AUTH_USER_FIELDS.contains(rc.getName())) {
                dataMap.put(rc.getName(), currentUserId);
            }
        }
    }

    private String getUserIdFromAuth(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserId();
        }
        return null;
    }
}
