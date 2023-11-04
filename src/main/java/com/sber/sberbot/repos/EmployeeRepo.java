package com.sber.sberbot.repos;

import com.sber.sberbot.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    Employee findByUsername(String username);
    Employee findByTgId(Long telegramId);
}
