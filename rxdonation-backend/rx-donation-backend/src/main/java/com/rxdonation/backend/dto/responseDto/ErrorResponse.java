package com.rxdonation.backend.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        Map<String, String> fieldErrors,
        String traceId
) {}
