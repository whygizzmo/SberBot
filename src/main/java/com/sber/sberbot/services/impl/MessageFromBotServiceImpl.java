package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.MessageFromBot;
import com.sber.sberbot.repos.MessageFromBotRepo;
import com.sber.sberbot.services.MessageFromBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageFromBotServiceImpl implements MessageFromBotService {
    private final MessageFromBotRepo messageFromBotRepo;
    @Override
    public MessageFromBot saveMessageFromBot(MessageFromBot message) {
        return messageFromBotRepo.save(message);
    }
}
