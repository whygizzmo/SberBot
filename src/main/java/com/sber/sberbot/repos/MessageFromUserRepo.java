package com.sber.sberbot.repos;

import com.sber.sberbot.models.MessageFromUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageFromUserRepo extends JpaRepository<MessageFromUser, Long> {
}
