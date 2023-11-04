package com.sber.sberbot.configs;

import com.sber.sberbot.models.Admin;
import com.sber.sberbot.models.Employee;
import com.sber.sberbot.models.MessageFromBot;
import com.sber.sberbot.models.MessageFromUser;
import com.sber.sberbot.models.dtos.FindEmployeeDto;
import com.sber.sberbot.models.enums.State;
import com.sber.sberbot.services.AdminService;
import com.sber.sberbot.services.EmployeeService;
import com.sber.sberbot.services.MessageFromBotService;
import com.sber.sberbot.services.MessageFromUserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    final MessageFromBotService messageFromBotService;

    public TgBot(BotConfig botConfig, AdminService adminService, EmployeeService employeeService, MessageFromUserService messageFromUserService, MessageFromBotService messageFromBotService) {
        this.botConfig = botConfig;
        this.adminService = adminService;
        this.employeeService = employeeService;
        this.messageFromUserService = messageFromUserService;
        this.messageFromBotService = messageFromBotService;
    }

    @Override
    public void onUpdateReceived(Update update) {


        try {

            String chatId = update.getMessage().getChatId().toString();
            String inMessage = update.getMessage().getText();

            if (update.hasMessage() && update.getMessage().hasText()) {
                FindEmployeeDto findEmployeeDto = new FindEmployeeDto();
                findEmployeeDto.setUsername(update.getMessage().getFrom().getUserName());
                findEmployeeDto.setChatId(Long.valueOf(chatId));

                Employee employee = employeeService.findOrCreateEmployee(findEmployeeDto);

                MessageFromUser messageFromUser = new MessageFromUser();
                messageFromUser.setMessageText(inMessage);
                messageFromUser.setEmployeeId(employee);
                messageFromUser.setMessageDate(LocalDateTime.now());
                messageFromUserService.createNewMessage(messageFromUser);

            }

            if (botState == State.WAITING_USERNAME) {
                botState = State.FREE;
                adminService.createNewAdmin(inMessage.trim(), update.getMessage().getChat().getUserName());
                execute(new SendMessage(chatId, "Админ добавлен"));
            }


            if (botState == State.FREE) {


                if (inMessage.equals("/addAdmin")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_USERNAME;
                        sendTextMessage(chatId, "Введите юзернейм нового админа");
                    } else {
                        sendTextMessage(chatId, "Вы не являетесь админом");
                    }

                }

            }
        } catch (Exception e) {
            //??? ?? ?????? ?????? ????. ?? ???? ????? ??? ?????? ????? ???????
            System.err.println("ERROR");
        }
        //?????? ??? ???? ?? ????? ???? ?? ??? ?????? ??????????

    }
    public void sendTextMessage(String chatId, String text){
        try {
            SendMessage sendMessage = new SendMessage();
            FindEmployeeDto findEmployeeDto = new FindEmployeeDto();

            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            execute(sendMessage);
            MessageFromBot message = new MessageFromBot();
            message.setMessageText(text);
            message.setId(Long.valueOf(chatId));
            message.setMessageDate(LocalDateTime.now());
            findEmployeeDto.setChatId(Long.valueOf(chatId));
            message.setEmployee(employeeService.findOrCreateEmployee(findEmployeeDto));
            messageFromBotService.saveMessageFromBot(message);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
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
