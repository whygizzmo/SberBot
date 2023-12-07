package com.sber.sberbot.repos;

import com.sber.sberbot.models.RegistrationForStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationForStudyRepo extends JpaRepository<RegistrationForStudy, Long> {
    @Query(value = "select * from registration_for_study where date_of_event < now() and is_inform = false ",nativeQuery = true)
    List<RegistrationForStudy> getAllActualStudy();

    //List<RegistrationForStudy> findAllByAppearedIsFalseAndInformIsTrueAndEmployee_TgId(Long tgId);
    List<RegistrationForStudy> findAllByIsAppearedFalseAndIsInformTrueAndEmployee_TgId(Long tgId);
    List<RegistrationForStudy> findAllByIsAppearedTrueAndIsWorkFalseAndEmployee_TgId(Long tgId);

}
