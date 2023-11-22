package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.Admin;
import com.sber.sberbot.models.Employee;
import com.sber.sberbot.repos.AdminRepo;
import com.sber.sberbot.repos.EmployeeRepo;
import com.sber.sberbot.services.AdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepo adminRepo;
    private final EmployeeRepo employeeRepo;


    public AdminServiceImpl(AdminRepo adminRepo, EmployeeRepo employeeRepo) {
        this.adminRepo = adminRepo;
        this.employeeRepo = employeeRepo;
    }

    @Override
    public List<Admin> getAll() {
        return adminRepo.findAll();
    }

    @Override
    public Admin createNewAdmin(String newAdminUsername, String adminUsername) {
        Employee newAdmin = employeeRepo.findByUsername(newAdminUsername);
        if (newAdmin == null) {
            return null;
        }
        Admin assignedByAdmin = adminRepo.findByEmployeeUsernameAndEndDateAfter(adminUsername,LocalDate.now());

        Admin admin = new Admin();
        admin.setEmployee(newAdmin);
        admin.setAdmin(assignedByAdmin);
        admin.setStartDate(LocalDate.now());
        admin.setEndDate(admin.getStartDate().plusYears(100));

        return adminRepo.save(admin);

    }

    @Override
    public Admin deleteAdmin(Long id) {
        Admin admin = adminRepo.findById(id).get();
        if (admin == null) {
            return null;
        }
        admin.setEndDate(LocalDate.now());
        adminRepo.save(admin);

        return admin;
    }

    @Override
    public String getAdmins() {
        List<Admin> admins = adminRepo.findAllByEndDateAfter(LocalDate.now());
        String adminsStr = "";
        for (int i = 0; i < admins.size(); i++) {
            adminsStr = adminsStr + (admins.get(i).getId() + ". " + admins.get(i).getEmployee().getUsername() + "\n");
        }
        return adminsStr;
    }


}
