package com.sber.sberbot.configs;

import com.sber.sberbot.models.Admin;
import com.sber.sberbot.models.Employee;
import com.sber.sberbot.models.MessageFromUser;
import com.sber.sberbot.models.enums.State;
import com.sber.sberbot.services.AdminService;
import com.sber.sberbot.services.EmployeeService;
import com.sber.sberbot.services.MessageFromUserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TgBot extends TelegramLongPollingBot {
    State botState = State.FREE;
    final BotConfig botConfig;
    final AdminService adminService;
    final EmployeeService employeeService;
    final MessageFromUserService messageFromUserService;

    public TgBot(BotConfig botConfig, AdminService adminService, EmployeeService employeeService, MessageFromUserService messageFromUserService) {
        this.botConfig = botConfig;
        this.adminService = adminService;
        this.employeeService = employeeService;
        this.messageFromUserService = messageFromUserService;
    }

    @Override
    public void onUpdateReceived(Update update) {


        try {

            String chatId = update.getMessage().getChatId().toString();
            String inMessage = update.getMessage().getText();

            if (update.hasMessage() && update.getMessage().hasText()) {

                Employee employee = employeeService.findOrCreateEmployee(Long.valueOf(chatId),update.getMessage().getFrom().getUserName());

                MessageFromUser messageFromUser = new MessageFromUser();
                messageFromUser.setMessageText(inMessage);
                messageFromUser.setEmployeeId(employee);
                messageFromUser.setMessageDate(LocalDateTime.now());
                messageFromUserService.createNewMessage(messageFromUser);

            }

            if (botState == State.WAITING_USERNAME) {
                botState = State.FREE;
                adminService.createNewAdmin(inMessage.trim(), update.getMessage().getChat().getUserName());
                execute(new SendMessage(chatId, "????? ????????"));
            }


            if (botState == State.FREE) {


                if (inMessage.equals("/addAdmin")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_USERNAME;
                        execute(new SendMessage(chatId, "??????? ???????? ?????? ??????"));
                    } else {
                        execute(new SendMessage(chatId, "?? ?? ????????? ???????"));
                    }

                }

            }
        } catch (Exception e) {
            //??? ?? ?????? ?????? ????. ?? ???? ????? ??? ?????? ????? ???????
            System.err.println("ERROR");
        }
        //?????? ??? ???? ?? ????? ???? ?? ??? ?????? ??????????

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
