package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.MessageFromUser;
import com.sber.sberbot.repos.MessageFromUserRepo;
import com.sber.sberbot.services.MessageFromUserService;
import org.springframework.stereotype.Service;

@Service
public class MessageFromUserServiceImpl implements MessageFromUserService {

    private final MessageFromUserRepo messageFromUserRepo;

    public MessageFromUserServiceImpl(MessageFromUserRepo messageFromUserRepo) {
        this.messageFromUserRepo = messageFromUserRepo;
    }

    @Override
    public MessageFromUser createNewMessage(MessageFromUser messageFromUser) {
        return messageFromUserRepo.save(messageFromUser);
    }
}
