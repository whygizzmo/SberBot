package com.sber.sberbot.services;

import com.sber.sberbot.models.RegistrationForStudy;

import java.util.List;

public interface RegistrationForStudyService {
    String addUsersToStudy(String idAndDate);

    String getStudyList();

    List<RegistrationForStudy> sendInform();

    void makeAppearedTrue(String chatTgId);
}
