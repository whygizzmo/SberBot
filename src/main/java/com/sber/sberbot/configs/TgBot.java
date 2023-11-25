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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        //сделать отправку сообщений от чела с датой и дроблением на смс
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
                messageFromUser.setEmployee(employee);
                messageFromUser.setMessageDate(LocalDateTime.now());
                messageFromUserService.createNewMessage(messageFromUser);

            }

            if (botState == State.WAITING_USERNAME_FOR_ADD_ADMIN) {
                botState = State.FREE;
                if (adminService.createNewAdmin(inMessage.trim(), update.getMessage().getChat().getUserName()) == null) {

                    sendTextMessage(chatId, "Юзер с таким ником не найден");

                } else {

                    sendTextMessage(chatId, "Админ добавлен");
                }
            } else if (botState == State.WAITING_ID_FOR_DELETE_ADMIN) {

                botState = State.FREE;

                if (adminService.deleteAdmin(Long.valueOf(inMessage)) == null) {

                    sendTextMessage(chatId, "Админ с таким id не найден");

                } else {

                    sendTextMessage(chatId, "Админ успешно удален");
                }

            } else if (botState == State.WAITING_ID_FOR_USERS_MESSAGE) {

                botState = State.FREE;

                String messageStr;

                if (botState == State.WAITING_ID_FOR_USERS_MESSAGE) {

                    botState = State.FREE;


                    if ((messageStr = messageFromUserService.getUserMessages(Long.valueOf(inMessage))) == null) {

                        sendTextMessage(chatId, "Пользователь с таким id не найден");

                    } else {

                        sendTextMessage(chatId, messageStr);
                    }
                }
            } else if (botState == State.WAITING_ID_FOR_CHANGE_USERS_STATUS) {
                botState = State.FREE;

                String messageStr;

                if ((messageStr = employeeService.changeEmployeeStatus(Long.valueOf(inMessage))) == null) {

                    sendTextMessage(chatId, "Пользователь с таким id не найден");

                } else {

                    sendTextMessage(chatId, messageStr);
                }

            } else if (botState == State.FREE) {


                if (inMessage.equals("/addAdmin")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_USERNAME_FOR_ADD_ADMIN;
                        sendTextMessage(chatId, "Введите юзернейм нового админа");
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }

                } else if (inMessage.equals("/admin")) {

                    sendAdminPanel(chatId);

                } else if (inMessage.equals("/deleteAdmin")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_ID_FOR_DELETE_ADMIN;
                        sendTextMessage(chatId, adminService.getAdmins() + "\nВведите id админа которого хотите удалить");
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                } else if (inMessage.equals("/баймайшегиба")) {

                    adminService.createNewAdmin(update.getMessage().getChat().getUserName(), null);
                    sendTextMessage(chatId, "\uD83D\uDE3C");

                } else if (inMessage.equals("/getAllUsers")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {
                        sendTextMessage(chatId, employeeService.getAllEmployees());
                    } else {
                        sendTextMessage(chatId, "Вы не являетесь админом");
                    }

                } else if (inMessage.equals("/getUserMessages")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_ID_FOR_USERS_MESSAGE;
                        sendTextMessage(chatId, employeeService.getAllEmployees() +
                                "\nВведите id пользователя чьи сообщения хотите получить :  ");
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }

                } else if (inMessage.equals("/changeUserStatus")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_ID_FOR_CHANGE_USERS_STATUS;
                        sendTextMessage(chatId, employeeService.getAllEmployees() +
                                "\n Введите id пользователя чей статус хотите поменять");

                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                }

            }
        } catch (Exception e) {
            //??? ?? ?????? ?????? ????. ?? ???? ????? ??? ?????? ????? ???????
            System.err.println(e);
            System.err.println("ERROR");
        }
        //?????? ??? ???? ?? ????? ???? ?? ??? ?????? ??????????

    }

    public void sendTextMessage(String chatId, String text) {
        FindEmployeeDto findEmployeeDto = new FindEmployeeDto();

        int over = 1200;

        if (text.length() > over) {

            int startIndex = 0;
            int endIndex = over;

            for (int i = 0; i <= text.length() / over; i++) {

                if (endIndex > text.length()) {
                    endIndex = text.length();
                }

                SendMessage sendMessage = new SendMessage();

                sendMessage.setChatId(chatId);
                sendMessage.setText(text.substring(startIndex, endIndex));

                try {
                    execute(sendMessage);
                    Thread.sleep(2000);

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


                MessageFromBot message = new MessageFromBot();
                message.setMessageText(text.substring(startIndex, endIndex).length() > 255 ?
                        text.substring(startIndex, startIndex + 255) : text.substring(startIndex, endIndex));
                message.setId(Long.valueOf(chatId));
                message.setMessageDate(LocalDateTime.now());
                findEmployeeDto.setChatId(Long.valueOf(chatId));
                message.setEmployee(employeeService.findOrCreateEmployee(findEmployeeDto));
                messageFromBotService.saveMessageFromBot(message);
                startIndex += over;
                endIndex += over;
            }
        } else {
            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            MessageFromBot message = new MessageFromBot();
            message.setMessageText(text);
            message.setId(Long.valueOf(chatId));
            message.setMessageDate(LocalDateTime.now());
            findEmployeeDto.setChatId(Long.valueOf(chatId));
            message.setEmployee(employeeService.findOrCreateEmployee(findEmployeeDto));
            messageFromBotService.saveMessageFromBot(message);


        }
    }

    public void sendTextMessage(SendMessage message) {
        FindEmployeeDto findEmployeeDto = new FindEmployeeDto();

        int over = 1200;

        if (message.getText().length() > over) {
            int startIndex = 0;
            int endIndex = over;

            for (int i = 0; i <= message.getText().length() / over; i++) {

                if (endIndex > message.getText().length()) {
                    endIndex = message.getText().length();
                }

                MessageFromBot messageFromBot = new MessageFromBot();
                messageFromBot.setMessageText(message.getText().substring(startIndex, endIndex).length() > 255 ?
                        message.getText().substring(startIndex, startIndex + 255) : message.getText().substring(startIndex, endIndex));
                messageFromBot.setId(Long.valueOf(message.getChatId()));
                messageFromBot.setMessageDate(LocalDateTime.now());
                findEmployeeDto.setChatId(Long.valueOf(message.getChatId()));
                messageFromBot.setEmployee(employeeService.findOrCreateEmployee(findEmployeeDto));
                messageFromBotService.saveMessageFromBot(messageFromBot);
                startIndex += over;
                endIndex += over;
                try {
                    execute(message);
                    Thread.sleep(2000);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


        } else {
            MessageFromBot messageFromBot = new MessageFromBot();

            messageFromBot.setMessageText(message.getText());
            messageFromBot.setId(Long.valueOf(message.getChatId()));
            messageFromBot.setMessageDate(LocalDateTime.now());
            findEmployeeDto.setChatId(Long.valueOf(message.getChatId()));
            messageFromBot.setEmployee(employeeService.findOrCreateEmployee(findEmployeeDto));
            messageFromBotService.saveMessageFromBot(messageFromBot);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void sendAdminPanel(String chatId) {
        List<Admin> admins = adminService.getAll();
        if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
                a.getEndDate().isAfter(LocalDate.now()))) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();

            // Команды для админ панели

            //кнопка AddAdmin
            KeyboardButton addAdminCommand = new KeyboardButton();
            addAdminCommand.setText("/addAdmin");
            row.add(addAdminCommand);
            //кнопка DeleteAdmin
            KeyboardButton deleteAdminCommand = new KeyboardButton();
            deleteAdminCommand.setText("/deleteAdmin");
            row.add(deleteAdminCommand);
            //кнопка getAllUsers
            KeyboardButton getAllUsersCommand = new KeyboardButton();
            getAllUsersCommand.setText("/getAllUsers");
            row.add(getAllUsersCommand);
            //кнопка getAllUsersMessage
            KeyboardButton getUserMessagesCommand = new KeyboardButton();
            getUserMessagesCommand.setText("/getUserMessages");
            row.add(getUserMessagesCommand);
            //кнопка changeUserStatus
            KeyboardButton changeUserStatusCommand = new KeyboardButton();
            changeUserStatusCommand.setText("/changeUserStatus");
            row.add(changeUserStatusCommand);


            keyboard.add(row);

            keyboardMarkup.setKeyboard(keyboard);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Административная панель:");
            message.setReplyMarkup(keyboardMarkup);

            sendTextMessage(message);
        } else {
            sendTextMessage(chatId, "Вы не являетесь админом");
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

