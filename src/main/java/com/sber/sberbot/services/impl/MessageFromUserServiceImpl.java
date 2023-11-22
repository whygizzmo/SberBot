package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.MessageFromUser;
import com.sber.sberbot.repos.MessageFromUserRepo;
import com.sber.sberbot.services.MessageFromUserService;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public String getUserMessages(Long employeeId) {
        List<MessageFromUser> messages = messageFromUserRepo.findByEmployee_Id(employeeId);
        if (messages == null) {
            return null;
        }
        String messagesStr = "Дата :                                           Сообщение :\n";
        for (int i = 0; i < messages.size(); i++) {
            messagesStr += "\n" + messages.get(i).getMessageDate() + "      ||      " + messages.get(i).getMessageText() + "\n";
        }

        return messagesStr;
    }
}
