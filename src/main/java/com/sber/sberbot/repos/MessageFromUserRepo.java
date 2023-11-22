package com.sber.sberbot.repos;

import com.sber.sberbot.models.MessageFromUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageFromUserRepo extends JpaRepository<MessageFromUser, Long> {
    List<MessageFromUser> findByEmployee_Id(Long id);
}
