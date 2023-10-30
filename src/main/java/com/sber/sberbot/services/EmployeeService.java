package com.sber.sberbot.services;

import com.sber.sberbot.models.Employee;

public interface EmployeeService {
    Employee findOrCreateEmployee(Long telegramId, String username);
}
