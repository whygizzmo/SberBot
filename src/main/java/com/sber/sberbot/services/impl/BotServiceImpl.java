package com.sber.sberbot.services.impl;

import com.sber.sberbot.configs.TgBot;
import com.sber.sberbot.services.BotService;
import org.springframework.stereotype.Service;

@Service
public class BotServiceImpl implements BotService {
    private TgBot tgBot;

    public BotServiceImpl(TgBot tgBot) {
        this.tgBot = tgBot;
    }

    @Override
    public void sendStudyQuiz() {
        tgBot.sendQuizStudy();
    }
}
