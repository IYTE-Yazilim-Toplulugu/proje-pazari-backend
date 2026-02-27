package com.iyte_yazilim.proje_pazari.domain.interfaces;

/**
 * Interface for validating commands before processing.
 *
 * <p>Validators are responsible for checking business rules that cannot be expressed through
 * annotation-based validation (e.g., {@code @NotBlank}).
 *
 * <h2>Example Implementation:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Component
 *     public class RegisterUserValidator implements IValidator<RegisterUserCommand> {
 *         @Override
 *         public String[] validate(RegisterUserCommand command) {
 *             List<String> errors = new ArrayList<>();
 *             if (!command.email().endsWith("@iyte.edu.tr")) {
 *                 errors.add("Email must be from IYTE domain");
 *             }
 *             return errors.toArray(new String[0]);
 *         }
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of command to validate
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 */
public interface IValidator<T> {

    /**
     * Validates the given command and returns any validation errors.
     *
     * @param command the command to validate
     * @return array of error messages, empty if validation passes
     */
    String[] validate(T command);
}
