package com.example.employee.repository;

import com.example.employee.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void saveEmployee() {
        Employee saved = employeeRepository.save(sampleEmployee("alex.morgan@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findEmployee() {
        Employee saved = employeeRepository.save(sampleEmployee("casey.lee@example.com"));

        assertThat(employeeRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Employee::getEmail)
                .isEqualTo("casey.lee@example.com");
    }

    @Test
    void deleteEmployee() {
        Employee saved = employeeRepository.save(sampleEmployee("jordan.kim@example.com"));

        employeeRepository.deleteById(saved.getId());

        assertThat(employeeRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void emailMustBeUnique() {
        employeeRepository.saveAndFlush(sampleEmployee("unique@example.com"));

        assertThatThrownBy(() -> employeeRepository.saveAndFlush(sampleEmployee("unique@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Employee sampleEmployee(String email) {
        return new Employee(
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
}
