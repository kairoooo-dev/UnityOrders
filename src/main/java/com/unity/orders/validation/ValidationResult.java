package com.unity.orders.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Immutable result of a validation check.
 *
 * <p>Contains a list of error messages (empty if validation passed).
 * Use {@link #isValid()} to check whether validation succeeded.</p>
 */
public final class ValidationResult {

    private final boolean valid;
    private final @NotNull @Unmodifiable List<String> errors;

    private ValidationResult(boolean valid, @NotNull List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(errors);
    }

    /**
     * Returns whether validation passed (no errors).
     *
     * @return {@code true} if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns whether validation failed.
     *
     * @return {@code true} if invalid
     */
    public boolean isInvalid() {
        return !valid;
    }

    /**
     * Returns the list of error messages.
     *
     * @return an unmodifiable list of errors (empty if valid)
     */
    public @NotNull @Unmodifiable List<String> getErrors() {
        return errors;
    }

    /**
     * Returns the first error message, or empty string if valid.
     *
     * @return the first error or empty string
     */
    public @NotNull String getFirstError() {
        return errors.isEmpty() ? "" : errors.get(0);
    }

    /**
     * Combines this result with another. The combined result is valid only if both are valid.
     *
     * @param other the other result
     * @return a combined result
     */
    public @NotNull ValidationResult combine(@NotNull ValidationResult other) {
        Objects.requireNonNull(other, "other must not be null");
        List<String> combinedErrors = new ArrayList<>(this.errors);
        combinedErrors.addAll(other.errors);
        return new ValidationResult(combinedErrors.isEmpty(), combinedErrors);
    }

    /**
     * Returns a successful validation result.
     *
     * @return a valid result
     */
    public static @NotNull ValidationResult valid() {
        return new ValidationResult(true, List.of());
    }

    /**
     * Returns a failed validation result with a single error.
     *
     * @param error the error message
     * @return an invalid result
     */
    public static @NotNull ValidationResult invalid(@NotNull String error) {
        return new ValidationResult(false, List.of(Objects.requireNonNull(error, "error must not be null")));
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ValidationResult}.
     */
    public static final class Builder {
        private final @NotNull List<String> errors = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an error message.
         *
         * @param error the error message
         * @return this builder
         */
        public @NotNull Builder addError(@NotNull String error) {
            errors.add(Objects.requireNonNull(error, "error must not be null"));
            return this;
        }

        /**
         * Builds the validation result.
         *
         * @return the result
         */
        public @NotNull ValidationResult build() {
            return new ValidationResult(errors.isEmpty(), new ArrayList<>(errors));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid && errors.equals(that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
