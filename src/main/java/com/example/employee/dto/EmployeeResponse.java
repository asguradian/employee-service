package com.example.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Employee representation returned by the API")
public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String department,
        String jobTitle,
        BigDecimal salary,
        LocalDate hireDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
