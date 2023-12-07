package com.sber.sberbot.services;

import com.sber.sberbot.configs.TgBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface BotService {

    void sendStudyQuiz();
}
