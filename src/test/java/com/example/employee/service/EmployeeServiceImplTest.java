package com.example.employee.service;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.entity.Employee;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository);
    }

    @Test
    void createEmployee() {
        Employee saved = employeeWithId(1L, "alex.morgan@example.com");
        when(employeeRepository.existsByEmail("alex.morgan@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeResponse response = employeeService.createEmployee(sampleRequest("alex.morgan@example.com"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("alex.morgan@example.com");
    }

    @Test
    void getEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employeeWithId(1L, "alex.morgan@example.com")));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void employeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee with id 99 was not found");
    }

    @Test
    void updateEmployee() {
        Employee existing = employeeWithId(1L, "old@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(employeeRepository.save(existing)).thenReturn(existing);

        EmployeeResponse response = employeeService.updateEmployee(1L, sampleRequest("new@example.com"));

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.firstName()).isEqualTo("Alex");
    }

    @Test
    void deleteEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteEmployeeNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void createEmployeeMapsRequestToEntity() {
        when(employeeRepository.existsByEmail("alex.morgan@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            setField(employee, "id", 1L);
            setField(employee, "createdAt", LocalDateTime.now());
            setField(employee, "updatedAt", LocalDateTime.now());
            return employee;
        });

        employeeService.createEmployee(sampleRequest("alex.morgan@example.com"));

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getSalary()).isEqualByComparingTo("120000.00");
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

    private Employee employeeWithId(Long id, String email) {
        Employee employee = new Employee(
                "Alex",
                "Morgan",
                email,
                "212-555-0100",
                "Engineering",
                "Software Engineer",
                new BigDecimal("120000.00"),
                LocalDate.of(2024, 2, 12)
        );
        setField(employee, "id", id);
        setField(employee, "createdAt", LocalDateTime.of(2024, 2, 12, 9, 0));
        setField(employee, "updatedAt", LocalDateTime.of(2024, 2, 12, 9, 0));
        return employee;
    }

    private void setField(Employee employee, String name, Object value) {
        try {
            Field field = Employee.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(employee, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
