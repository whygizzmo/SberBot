package com.sber.sberbot.configs;

import com.sber.sberbot.models.*;
import com.sber.sberbot.models.dtos.FindEmployeeDto;
import com.sber.sberbot.models.enums.State;
import com.sber.sberbot.services.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TgBot extends TelegramLongPollingBot {
    State botState = State.FREE;
    Employee employee = new Employee();
    final BotConfig botConfig;
    final AdminService adminService;
    final EmployeeService employeeService;
    final MessageFromUserService messageFromUserService;
    final MessageFromBotService messageFromBotService;
    final RegistrationForStudyService registrationForStudyService;

    public TgBot(BotConfig botConfig, AdminService adminService, EmployeeService employeeService, MessageFromUserService messageFromUserService, MessageFromBotService messageFromBotService, RegistrationForStudyService registrationForStudyService) {
        this.botConfig = botConfig;
        this.adminService = adminService;
        this.employeeService = employeeService;
        this.messageFromUserService = messageFromUserService;
        this.messageFromBotService = messageFromBotService;
        this.registrationForStudyService = registrationForStudyService;
    }

    @Override
    public void onUpdateReceived(Update update) {

        try {

            String chatId = update.hasMessage() ? update.getMessage().getChatId().toString() : update.getCallbackQuery().getFrom().getId().toString();
            String inMessage = update.hasMessage() && update.getMessage().hasText() ? update.getMessage().getText() : "";

            if (update.hasMessage() && update.getMessage().hasText()) {
                FindEmployeeDto findEmployeeDto = new FindEmployeeDto();
                findEmployeeDto.setUsername(update.getMessage().getFrom().getUserName());
                findEmployeeDto.setChatId(Long.valueOf(chatId));

                 employee = employeeService.findOrCreateEmployee(findEmployeeDto);

                MessageFromUser messageFromUser = new MessageFromUser();
                messageFromUser.setMessageText(inMessage);
                messageFromUser.setEmployee(employee);
                messageFromUser.setMessageDate(LocalDateTime.now());
                messageFromUserService.createNewMessage(messageFromUser);

            }
            if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();

                if (data.equals("present_from_studies")) {
                    registrationForStudyService.makeAppearedTrue(chatId);
                    sendWorkPanel(chatId);
                } else if (data.equals("absent_from_studies")) {
                    sendTextMessage(chatId, "Обратитесь к менеджеру");
                } else if (data.equals("present_from_work")) {
                    registrationForStudyService.makeWorkTrue(chatId);
                    sendTextMessage(chatId, "Позравляем, успешной работы!");
                } else if (data.equals("absent_from_work")) {
                    sendTextMessage(chatId, "Обратитесь к менеджеру");
                }

            }

            if (employee.getStatusTg() == State.WAITING_USERNAME_FOR_ADD_ADMIN) {

                employee.setStatusTg(State.FREE);
                employeeService.update(employee);

                if (adminService.createNewAdmin(inMessage.trim(), update.getMessage().getChat().getUserName()) == null) {

                    sendTextMessage(chatId, "Юзер с таким ником не найден");

                } else {

                    sendTextMessage(chatId, "Админ добавлен");
                }
            } else if (employee.getStatusTg() == State.WAITING_ID_FOR_DELETE_ADMIN) {

                employee.setStatusTg(State.FREE);
                employeeService.update(employee);

                if (adminService.deleteAdmin(Long.valueOf(inMessage)) == null) {

                    sendTextMessage(chatId, "Админ с таким id не найден");

                } else {

                    sendTextMessage(chatId, "Админ успешно удален");
                }

            } else if (employee.getStatusTg() == State.WAITING_ID_FOR_USERS_MESSAGE) {

                employee.setStatusTg(State.FREE);
                employeeService.update(employee);

                String messageStr;

                if ((messageStr = messageFromUserService.getUserMessages(Long.valueOf(inMessage))) == null) {

                    sendTextMessage(chatId, "Пользователь с таким id не найден");

                } else {

                    sendTextMessage(chatId, messageStr);
                }

            } else if (employee.getStatusTg() == State.WAITING_ID_FOR_CHANGE_USERS_STATUS) {

                employee.setStatusTg(State.FREE);
                employeeService.update(employee);

                String messageStr;

                if ((messageStr = employeeService.changeEmployeeStatus(Long.valueOf(inMessage))) == null) {

                    sendTextMessage(chatId, "Пользователь с таким id не найден");

                } else {

                    sendTextMessage(chatId, messageStr);
                }

            } else if (employee.getStatusTg() == State.WAITING_ID_FOR_ADD_USERS_TO_STUDY) {

                employee.setStatusTg(State.FREE);
                employeeService.update(employee);

                String messageStr;
                if ((messageStr = registrationForStudyService.addUsersToStudy(inMessage.
                        replaceAll("\\s+", ""))) == null) { // удаляет все пробелы
                    sendTextMessage(chatId, "Не найден пользователь с таким айди или не правильно указана дата");
                } else {
                    sendTextMessage(chatId, messageStr);
                }

            } else if (employee.getStatusTg() == State.FREE) {


                if (inMessage.equals("/Добавить_Админа")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        employee.setStatusTg(State.WAITING_USERNAME_FOR_ADD_ADMIN);
                        employeeService.update(employee);

                        sendTextMessage(chatId, "Введите юзернейм нового админа");
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }

                } else if (inMessage.equals("/Список_Админов")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        sendTextMessage(chatId, "Список администраторов : \n\n" + adminService.getAdmins());
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                } else if (inMessage.equals("/admin")) {

                    sendAdminPanel(chatId);

                } else if (inMessage.equals("/Удалить_Админа")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        employee.setStatusTg(State.WAITING_ID_FOR_DELETE_ADMIN);
                        employeeService.update(employee);

                        sendTextMessage(chatId, adminService.getAdmins() + "\nВведите id админа которого хотите удалить");
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                } else if (inMessage.equals("/баймайшегиба")) {

                    adminService.createNewAdmin(update.getMessage().getChat().getUserName(), null);
                    sendTextMessage(chatId, "\uD83D\uDE3C");

                } else if (inMessage.equals("/Все_Пользователи")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {
                        sendTextMessage(chatId, employeeService.getAllEmployees());
                    } else {
                        sendTextMessage(chatId, "Вы не являетесь админом");
                    }

                } else if (inMessage.equals("/Сообщения_от_пользователя")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        employee.setStatusTg(State.WAITING_ID_FOR_USERS_MESSAGE);
                        employeeService.update(employee);

                        sendTextMessage(chatId, employeeService.getAllEmployees() +
                                "\nВведите id пользователя чьи сообщения хотите получить :  ");
                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }

                } else if (inMessage.equals("/Изменить_статус_польз-ля")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        employee.setStatusTg(State.WAITING_ID_FOR_CHANGE_USERS_STATUS);
                        employeeService.update(employee);


                        sendTextMessage(chatId, employeeService.getAllEmployees() +
                                "\n Введите id пользователя чей статус хотите поменять");

                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                } else if (inMessage.equals("/Добавить_на_обучение")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {

                        employee.setStatusTg(State.WAITING_ID_FOR_ADD_USERS_TO_STUDY);
                        employeeService.update(employee);


                        sendTextMessage(chatId, employeeService.getAllEmployees() +
                                "\n Введите id пользователя которого хотите записать на обучение и " +
                                "установите дату через ; . Например - 3;31-12-2023");

                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                } else if (inMessage.equals("/Список_обучения")) {
                    List<Admin> admins = adminService.getAll();
                    if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) &&
                            a.getEndDate().isAfter(LocalDate.now()))) {


                        sendTextMessage(chatId, registrationForStudyService.getStudyList());

                    } else {

                        sendTextMessage(chatId, "Вы не являетесь админом");

                    }
                } else if (inMessage.equals("/пуш")) {
                    sendQuizStudy();
                }

            }
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("ERROR");
        }

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
            message.setMessageText(text.substring(0, text.length()).length() > 255 ?
                    text.substring(0, 255) : text);
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

    public void sendQuizStudy() {
        List<RegistrationForStudy> studies = registrationForStudyService.sendInform();
        for (int i = 0; i < studies.size(); i++) {
            sendStudyPanel(studies.get(i).getEmployee().getTgId().toString());
        }
    }

    public void sendAdminPanel(String chatId) {
        List<Admin> admins = adminService.getAll();
        if (admins.stream().anyMatch(a -> a.getEmployee().getTgId().toString().equals(chatId) ||
                a.getEndDate().isAfter(LocalDate.now()))) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setSelective(true);
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(false);

            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();

            // Команды для админ панели

            KeyboardButton addAdminCommand = new KeyboardButton();
            addAdminCommand.setText("/Добавить_Админа");
            row.add(addAdminCommand);

            KeyboardButton deleteAdminCommand = new KeyboardButton();
            deleteAdminCommand.setText("/Удалить_Админа");
            row.add(deleteAdminCommand);

            KeyboardButton getAllUsersCommand = new KeyboardButton();
            getAllUsersCommand.setText("/Все_Пользователи");
            row2.add(getAllUsersCommand);

            //кнопка getAllUsersMessage
            KeyboardButton getUserMessagesCommand = new KeyboardButton();
            getUserMessagesCommand.setText("/Сообщения_от_пользователя");
            row2.add(getUserMessagesCommand);
            //кнопка changeUserStatus
            KeyboardButton changeUserStatusCommand = new KeyboardButton();
            changeUserStatusCommand.setText("/Изменить_статус_польз-ля");
            row3.add(changeUserStatusCommand);
            //кнопка addUserToStudyEvent
            KeyboardButton addUserToStudyCommand = new KeyboardButton();
            addUserToStudyCommand.setText("/Добавить_на_обучение");
            row3.add(addUserToStudyCommand);
            //кнопка getStudyList
            KeyboardButton getStudyListCommand = new KeyboardButton();
            getStudyListCommand.setText("/Список_обучения");
            row4.add(getStudyListCommand);
            //кнопка скинутьОпрос
            KeyboardButton adc2Command = new KeyboardButton();
            adc2Command.setText("/Список_Админов");
            row4.add(adc2Command);

            // Добавьте другие команды, как вам необходимо

            keyboard.add(row);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
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

    public void sendStudyPanel(String chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();


        InlineKeyboardButton presentCommand = new InlineKeyboardButton();
        presentCommand.setText("Да");
        presentCommand.setCallbackData("present_from_studies");

        InlineKeyboardButton absentCommand = new InlineKeyboardButton();
        absentCommand.setText("Нет");
        absentCommand.setCallbackData("absent_from_studies");

        List<InlineKeyboardButton> row1 = Collections.singletonList(presentCommand);
        List<InlineKeyboardButton> row2 = Collections.singletonList(absentCommand);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы присутствовали на обучении?");
        message.setReplyMarkup(inlineKeyboardMarkup);

        sendTextMessage(message);

    }

    public void sendWorkPanel(String chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();


        InlineKeyboardButton presentCommand = new InlineKeyboardButton();
        presentCommand.setText("Да");
        presentCommand.setCallbackData("present_from_work");

        InlineKeyboardButton absentCommand = new InlineKeyboardButton();
        absentCommand.setText("Нет");
        absentCommand.setCallbackData("absent_from_work");

        List<InlineKeyboardButton> row1 = Collections.singletonList(presentCommand);
        List<InlineKeyboardButton> row2 = Collections.singletonList(absentCommand);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы поставили первую смену?");
        message.setReplyMarkup(inlineKeyboardMarkup);

        sendTextMessage(message);

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

