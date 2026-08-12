package com.example.employee.service;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.entity.Employee;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException("Employee email must be unique");
        }
        Employee employee = toEntity(request);
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return toResponse(findEmployee(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        if (employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DataIntegrityViolationException("Employee email must be unique");
        }

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDepartment(request.department());
        employee.setJobTitle(request.jobTitle());
        employee.setSalary(request.salary());
        employee.setHireDate(request.hireDate());

        return toResponse(employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private Employee toEntity(EmployeeRequest request) {
        return new Employee(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                request.department(),
                request.jobTitle(),
                request.salary(),
                request.hireDate()
        );
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getDepartment(),
                employee.getJobTitle(),
                employee.getSalary(),
                employee.getHireDate(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
