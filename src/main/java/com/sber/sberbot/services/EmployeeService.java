package com.sber.sberbot.services;

import com.sber.sberbot.models.Employee;
import com.sber.sberbot.models.dtos.FindEmployeeDto;

public interface EmployeeService {
     String changeEmployeeStatus(Long id);

    Employee findOrCreateEmployee(FindEmployeeDto findEmployeeDto);

    String getAllEmployees();
}
