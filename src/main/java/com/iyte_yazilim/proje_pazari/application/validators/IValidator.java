package com.iyte_yazilim.proje_pazari.application.validators;

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
 * <h2>Transaction Constraint:</h2>
 *
 * <p>Implementations are invoked by {@code CustomValidationBehavior} ({@code @Order(3)}), which
 * runs <strong>before</strong> {@code TransactionBehavior} ({@code @Order(4)}). This means
 * validators execute outside any active transaction. Do not perform transactional writes inside a
 * validator. Read-only repository queries are acceptable, but they run without a transaction
 * context. If a transactional read is required, use {@code Propagation.SUPPORTS} on the repository
 * method, or move the check into the handler body which executes inside the transaction.
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
