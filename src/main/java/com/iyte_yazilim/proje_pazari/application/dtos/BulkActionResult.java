package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Result of a bulk operation")
@Getter
@Setter
public class BulkActionResult {

    @Schema(description = "Number of successful operations")
    private int successCount;

    @Schema(description = "Number of failed operations")
    private int failureCount;

    @Schema(description = "List of failures with details")
    private List<FailureDetail> failures = new ArrayList<>();

    public void incrementSuccess() {
        successCount++;
    }

    public void addFailure(String id, String reason) {
        failureCount++;
        failures.add(new FailureDetail(id, reason));
    }

    @Schema(description = "Details of a failed operation")
    public record FailureDetail(
            @Schema(description = "ID of the failed entity") String id,
            @Schema(description = "Reason for failure") String reason) {}
}
