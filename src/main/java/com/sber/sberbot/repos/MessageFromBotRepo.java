package com.sber.sberbot.repos;

import com.sber.sberbot.models.MessageFromBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageFromBotRepo extends JpaRepository<MessageFromBot, Long> {
}
