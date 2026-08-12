package com.example.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload used to create or update an employee")
public record EmployeeRequest(
        @NotBlank(message = "firstName is required")
        @Size(max = 100, message = "firstName must be at most 100 characters")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Size(max = 100, message = "lastName must be at most 100 characters")
        String lastName,

        @NotBlank(message = "email is required")
        @Email(message = "must be a valid email address")
        @Size(max = 255, message = "email must be at most 255 characters")
        String email,

        @Size(max = 30, message = "phoneNumber must be at most 30 characters")
        String phoneNumber,

        @Size(max = 100, message = "department must be at most 100 characters")
        String department,

        @Size(max = 100, message = "jobTitle must be at most 100 characters")
        String jobTitle,

        @NotNull(message = "salary is required")
        @DecimalMin(value = "0.00", message = "must be greater than or equal to 0")
        BigDecimal salary,

        @NotNull(message = "hireDate is required")
        LocalDate hireDate
) {
}
