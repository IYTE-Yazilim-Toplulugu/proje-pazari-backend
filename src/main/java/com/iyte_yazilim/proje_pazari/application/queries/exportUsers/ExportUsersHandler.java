package com.iyte_yazilim.proje_pazari.application.queries.exportUsers;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
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
                    .append( // Note: HashSet iteration order is not guaranteed; role ordering may
                            // vary
                            // across JVM runs (e.g., "USER|ADMIN" vs "ADMIN|USER"). This is
                            // acceptable
                            // since the import parser does not handle pipe-delimited multi-role
                            // values,
                            // and users are currently restricted to a single role via clear() +
                            // add() pattern.
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
