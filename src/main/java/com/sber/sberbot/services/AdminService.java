package com.sber.sberbot.services;

import com.sber.sberbot.models.Admin;

import java.util.List;

public interface AdminService {
    List<Admin> getAll();

    Admin createNewAdmin(String username, String adminUsername);

    String getAdmins();

    Admin deleteAdmin(Long id);
}
