package com.iyte_yazilim.proje_pazari.application.queries.exportApplications;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExportApplicationsHandler
        implements IRequestHandler<ExportApplicationsQuery, ApiResponse<String>> {

    private final ProjectApplicationRepository applicationRepository;

    @Override
    public ApiResponse<String> handle(ExportApplicationsQuery query) {
        List<ProjectApplicationEntity> applications = applicationRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("id,projectId,applicantId,status,createdAt,updatedAt\n");

        for (ProjectApplicationEntity app : applications) {
            csv.append(escapeCsv(app.getId()))
                    .append(",")
                    .append(app.getProject() != null ? escapeCsv(app.getProject().getId()) : "")
                    .append(",")
                    .append(app.getUser() != null ? escapeCsv(app.getUser().getId()) : "")
                    .append(",")
                    .append(app.getStatus() != null ? app.getStatus().name() : "")
                    .append(",")
                    .append(app.getCreatedAt() != null ? app.getCreatedAt().toString() : "")
                    .append(",")
                    .append(app.getUpdatedAt() != null ? app.getUpdatedAt().toString() : "")
                    .append("\n");
        }

        return ApiResponse.success(csv.toString(), "Applications exported successfully");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
