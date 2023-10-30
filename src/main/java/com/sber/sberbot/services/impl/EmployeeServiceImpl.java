package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.Employee;
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
    public Employee findOrCreateEmployee(Long telegramId, String username) {

        Employee employee = employeeRepo.findByTgId(telegramId);
        if (employee == null) {
            employee = new Employee();
            employee.setActive(true);
            employee.setTgId(telegramId);
            employee.setUsername(username);

            return employeeRepo.save(employee);
        }

        return employee;
    }
}
