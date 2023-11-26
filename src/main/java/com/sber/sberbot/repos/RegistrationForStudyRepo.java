package com.sber.sberbot.repos;

import com.sber.sberbot.models.RegistrationForStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationForStudyRepo extends JpaRepository<RegistrationForStudy, Long> {
}
