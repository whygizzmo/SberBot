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
                if (adminService.deleteAdmin(Long.valueOf(inMessage)) == null){
                    sendTextMessage(chatId, "Админ с таким ником не найден");
                }else {
                    sendTextMessage(chatId,"Админ успешно удален");
                }

            } else if (botState == State.FREE) {


                if (inMessage.equals("/addAdmin")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
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
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        botState = State.WAITING_ID_FOR_DELETE_ADMIN;
                        sendTextMessage(chatId, adminService.getAdmins() + "\nВведите id админа которого хотите удалить");
                    } else {
                        sendTextMessage(chatId, "Вы не являетесь админом");
                    }
                } else if (inMessage.equals("/баймайшегиба")) {
                    adminService.createNewAdmin(update.getMessage().getChat().getUserName(),null);
                    sendTextMessage(chatId,"\uD83D\uDE3C");
                } else if (inMessage.equals("/getAllUsers")) {
                    sendTextMessage(chatId,employeeService.getAllEmployes());
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

    public void sendTextMessage(SendMessage message) {
        MessageFromBot messageFromBot = new MessageFromBot();
        FindEmployeeDto findEmployeeDto = new FindEmployeeDto();

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
            deleteAdminCommand.setText("/getAllUsers");
            row.add(deleteAdminCommand);


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

