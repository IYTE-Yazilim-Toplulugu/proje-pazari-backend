package com.iyte_yazilim.proje_pazari.application.queries.exportUsers;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExportUsersHandler implements IRequestHandler<ExportUsersQuery, ApiResponse<String>> {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<String> handle(ExportUsersQuery query) {
        List<UserEntity> users = userRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("id,email,firstName,lastName,role,isActive,createdAt,updatedAt\n");

        for (UserEntity user : users) {
            csv.append(escapeCsv(user.getId()))
                    .append(",")
                    .append(escapeCsv(user.getEmail()))
                    .append(",")
                    .append(escapeCsv(user.getFirstName()))
                    .append(",")
                    .append(escapeCsv(user.getLastName()))
                    .append(",")
                    .append(
                            user.getRoles().stream()
                                    .map(Enum::name)
                                    .reduce((a, b) -> a + "|" + b)
                                    .orElse(""))
                    .append(",")
                    .append(user.getIsActive() != null ? user.getIsActive() : "")
                    .append(",")
                    .append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                    .append(",")
                    .append(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "")
                    .append("\n");
        }

        return ApiResponse.success(csv.toString(), "Users exported successfully");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
