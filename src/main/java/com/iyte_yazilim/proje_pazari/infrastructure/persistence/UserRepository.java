package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA Repository for user persistence operations.
 *
 * <p>Provides CRUD operations and custom queries for user entities.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see UserEntity
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email address to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Counts the number of projects owned by a user.
     *
     * @param userId the user's ULID
     * @return count of projects owned by the user
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.owner.id = :userId")
    int countProjectsByUserId(@Param("userId") String userId);

    /**
     * Counts the number of project applications submitted by a user.
     *
     * @param userId the user's ULID
     * @return count of applications by the user
     */
    @Query("SELECT COUNT(a) FROM ProjectApplicationEntity a WHERE a.user.id = :userId")
    int countApplicationsByUserId(@Param("userId") String userId);

    // --- Admin query methods ---

    Page<UserEntity> findByRole(RoleType role, Pageable pageable);

    Page<UserEntity> findByIsActive(Boolean isActive, Pageable pageable);

    long countByRole(RoleType role);

    long countByIsActive(Boolean isActive);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query(
            "SELECT u FROM UserEntity u WHERE "
                    + "(:role IS NULL OR u.role = :role) AND "
                    + "(:isActive IS NULL OR u.isActive = :isActive) AND "
                    + "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserEntity> findWithFilters(
            @Param("role") RoleType role,
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable);
}
