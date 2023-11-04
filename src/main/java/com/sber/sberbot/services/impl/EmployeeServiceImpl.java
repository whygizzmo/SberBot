package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.Employee;
import com.sber.sberbot.models.dtos.FindEmployeeDto;
import com.sber.sberbot.repos.EmployeeRepo;
import com.sber.sberbot.services.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    final EmployeeRepo employeeRepo;

    public EmployeeServiceImpl(EmployeeRepo employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    @Override
    public Employee findOrCreateEmployee(FindEmployeeDto findEmployeeDto) {

        Employee employee = employeeRepo.findByTgId(findEmployeeDto.getChatId());
        if (employee == null) {
            employee = new Employee();
            employee.setActive(true);
            employee.setTgId(findEmployeeDto.getChatId());
            employee.setUsername(findEmployeeDto.getUsername());

            return employeeRepo.save(employee);
        }

        return employee;
    }
}
