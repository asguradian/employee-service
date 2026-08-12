package com.example.employee.controller;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void postEmployee() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson("alex.morgan@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/employees/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("alex.morgan@example.com"));
    }

    @Test
    void getEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleResponse(1L), sampleResponse(2L)));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void putEmployee() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(sampleResponse(1L));

        mockMvc.perform(put("/api/v1/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleRequestJson("alex.morgan@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void validationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Morgan",
                                  "email": "not-an-email",
                                  "salary": -1.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists())
                .andExpect(jsonPath("$.fieldErrors.hireDate").exists());
    }

    @Test
    void employeeNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/v1/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employee Not Found"))
                .andExpect(jsonPath("$.message").value("Employee with id 99 was not found"));
    }

    private EmployeeRequest sampleRequest(String email) {
        return new EmployeeRequest(
                "Alex",
                "Morgan",
                email,
                "212-555-0100",
                "Engineering",
                "Software Engineer",
                new BigDecimal("120000.00"),
                LocalDate.of(2024, 2, 12)
        );
    }

    private String sampleRequestJson(String email) {
        return """
                {
                  "firstName": "Alex",
                  "lastName": "Morgan",
                  "email": "%s",
                  "phoneNumber": "212-555-0100",
                  "department": "Engineering",
                  "jobTitle": "Software Engineer",
                  "salary": 120000.00,
                  "hireDate": "2024-02-12"
                }
                """.formatted(email);
    }

    private EmployeeResponse sampleResponse(Long id) {
        return new EmployeeResponse(
                id,
                "Alex",
                "Morgan",
                "alex.morgan@example.com",
                "212-555-0100",
                "Engineering",
                "Software Engineer",
                new BigDecimal("120000.00"),
                LocalDate.of(2024, 2, 12),
                LocalDateTime.of(2024, 2, 12, 9, 0),
                LocalDateTime.of(2024, 2, 12, 9, 0)
        );
    }
}
