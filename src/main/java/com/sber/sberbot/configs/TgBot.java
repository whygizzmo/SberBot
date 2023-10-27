package com.sber.sberbot.configs;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TgBot extends TelegramLongPollingBot {
    final BotConfig botConfig;

    public TgBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }


    @Override
    public String getBotUsername() {
        return botConfig.botName;
    }

    @Override
    public String getBotToken() {
        return botConfig.botToken;
    }
}
