package com.sber.sberbot.configs;

import com.sber.sberbot.models.Admin;
import com.sber.sberbot.models.MessageFromUser;
import com.sber.sberbot.models.enums.State;
import com.sber.sberbot.repos.MessageFromUserRepo;
import com.sber.sberbot.services.AdminService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Component
public class TgBot extends TelegramLongPollingBot {
    State botState;
    final BotConfig botConfig;
    final AdminService adminService;
    private final MessageFromUserRepo messageFromUserRepo;

    public TgBot(BotConfig botConfig, AdminService adminService, MessageFromUserRepo messageFromUserRepo) {
        this.botConfig = botConfig;
        this.adminService = adminService;
        this.messageFromUserRepo = messageFromUserRepo;
    }

    @Override
    public void onUpdateReceived(Update update) {




        try {

            String chatId = update.getMessage().getChatId().toString();
            String inMessage = update.getMessage().getText();

            if (botState == State.WAITING_USERNAME) {
                adminService.createNewAdmin(inMessage.trim(), update.getMessage().getChat().getUserName());
                botState = State.FREE;
                execute(new SendMessage(chatId, "Админ добавлен"));
            }


            if (botState == State.FREE) {


                if (inMessage.equals("/addAdmin")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_USERNAME;
                        execute(new SendMessage(chatId, "Введите юзернейм нового админа"));
                    }else {
                        execute(new SendMessage(chatId,"Вы не являетесь админом"));
                    }

                }

            }
        } catch (Exception e) {
            //это на всякий случай пока. мб логи будут или просто пусто оставим
            System.err.println("ERROR");
        }
            //вообще это надо на самый верх но там ошибку показывает
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            Integer messageDate = update.getMessage().getDate();
            MessageFromUser messageFromUser = new MessageFromUser();
            messageFromUser.setMessageText(messageText);
            messageFromUser.setEmployeeId(chatId);
            messageFromUser.setMessageDate(messageDate);
            messageFromUserRepo.save(messageFromUser);

        }
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
