package com.sber.sberbot.services;

import com.sber.sberbot.models.MessageFromBot;

public interface MessageFromBotService {
    MessageFromBot saveMessageFromBot(MessageFromBot message);
}
