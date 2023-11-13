package com.sber.sberbot.repos;

import com.sber.sberbot.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {
    List<Admin> findAllByEndDateAfter(LocalDate date);
    Admin findByEmployeeUsername(String username);
}
