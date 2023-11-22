package com.sber.sberbot.services;

import com.sber.sberbot.models.MessageFromUser;

public interface MessageFromUserService {
    MessageFromUser createNewMessage(MessageFromUser messageFromUser);

    String getUserMessages(Long valueOf);
}
