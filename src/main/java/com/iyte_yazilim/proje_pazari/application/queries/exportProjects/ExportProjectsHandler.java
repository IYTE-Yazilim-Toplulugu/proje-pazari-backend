package com.iyte_yazilim.proje_pazari.application.queries.exportProjects;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExportProjectsHandler
        implements IRequestHandler<ExportProjectsQuery, ApiResponse<String>> {

    private final ProjectRepository projectRepository;

    @Override
    public ApiResponse<String> handle(ExportProjectsQuery query) {
        List<ProjectEntity> projects = projectRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append(
                "id,title,description,status,category,ownerId,maxTeamSize,featured,createdAt,updatedAt\n");

        for (ProjectEntity project : projects) {
            csv.append(escapeCsv(project.getId()))
                    .append(",")
                    .append(escapeCsv(project.getTitle()))
                    .append(",")
                    .append(escapeCsv(project.getDescription()))
                    .append(",")
                    .append(project.getStatus() != null ? project.getStatus().name() : "")
                    .append(",")
                    .append(escapeCsv(project.getCategory()))
                    .append(",")
                    .append(project.getOwner() != null ? escapeCsv(project.getOwner().getId()) : "")
                    .append(",")
                    .append(project.getMaxTeamSize() != null ? project.getMaxTeamSize() : "")
                    .append(",")
                    .append(project.isFeatured())
                    .append(",")
                    .append(project.getCreatedAt() != null ? project.getCreatedAt().toString() : "")
                    .append(",")
                    .append(project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : "")
                    .append("\n");
        }

        return ApiResponse.success(csv.toString(), "Projects exported successfully");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
