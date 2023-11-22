package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.Employee;
import com.sber.sberbot.models.dtos.FindEmployeeDto;
import com.sber.sberbot.repos.EmployeeRepo;
import com.sber.sberbot.services.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    final EmployeeRepo employeeRepo;

    public EmployeeServiceImpl(EmployeeRepo employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    @Override
    public String changeEmployeeStatus(Long id) {
        Employee employee = employeeRepo.findById(id).get();
        if (employee == null) {
            return null;
        }
        if (employee.isActive()) {
            employee.setActive(false);
        } else {
            employee.setActive(true);
        }
        employeeRepo.save(employee);
        return "Статус пользователя успешно изменен";
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

    @Override
    public String getAllEmployees() {
        List<Employee> employees = employeeRepo.findAll();
        String employeesStr = "";
        for (int i = 0; i < employees.size(); i++) {
            employeesStr += employees.get(i).getId() +
                    ". " + employees.get(i).getUsername() +
                    "   " + employees.get(i).isActive() + "\n";
        }
        return employeesStr;
    }
}
