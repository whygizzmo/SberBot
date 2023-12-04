package com.sber.sberbot.services.impl;

import com.sber.sberbot.models.Employee;
import com.sber.sberbot.models.RegistrationForStudy;
import com.sber.sberbot.repos.EmployeeRepo;
import com.sber.sberbot.repos.RegistrationForStudyRepo;
import com.sber.sberbot.services.RegistrationForStudyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistrationForStudyServiceImpl implements RegistrationForStudyService {

    private final EmployeeRepo employeeRepo;
    private final RegistrationForStudyRepo registrationForStudyRepo;

    public RegistrationForStudyServiceImpl(EmployeeRepo employeeRepo, RegistrationForStudyRepo registrationForStudyRepo) {
        this.employeeRepo = employeeRepo;
        this.registrationForStudyRepo = registrationForStudyRepo;
    }

    @Override
    public String addUsersToStudy(String idAndDate) {
        String input = idAndDate;

        // Регулярное выражение для поиска цифры и следующей за ней даты в формате "день-месяц-год"
        String regex = "\\d;\\d{2}-\\d{2}-\\d{4}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        int indexAfterId = input.indexOf(";");
        Long idInput = Long.valueOf(input.substring(0, indexAfterId));
        String dateString = input.substring(indexAfterId + 1);
        System.err.println(dateString);


        System.err.println(input.substring(0, indexAfterId));
        Employee employee = employeeRepo.findById(idInput).get();


        if (!matcher.find() || employee == null) {
            return null;
        } else {
            RegistrationForStudy registrationForStudy = new RegistrationForStudy();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            LocalDate date = LocalDate.parse(dateString, formatter);
            System.err.println(date);

            registrationForStudy.setEmployee(employee);
            registrationForStudy.setDateOfEvent(date);

            registrationForStudyRepo.save(registrationForStudy);

            return "Пользователь успешно записан на обучение";
        }
    }

    @Override
    public String getStudyList() {
        List<RegistrationForStudy> studies = registrationForStudyRepo.findAll();
        String output = "Список записей на обучение : \n  ID, дата, присутсвовал, получил оповещение, пользователь(id).\n\n";

        for (RegistrationForStudy reg : studies) {
            output += reg.getId() + " || " + reg.getDateOfEvent() + " || " + reg.isAppeared() + " || " + reg.isInform() +
                    " || " + reg.getEmployee().getUsername() + "(" + reg.getEmployee().getId() + ")\n";
        }

        return output;
    }

    @Override
    public List<RegistrationForStudy> sendInform() {
        List<RegistrationForStudy> studies = registrationForStudyRepo.getAllActualStudy();
        System.err.println(studies);
        for (RegistrationForStudy r : studies) {
            r.setInform(true);
        }
        registrationForStudyRepo.saveAll(studies);
        return studies;
    }

    @Override
    public void makeAppearedTrue(String chatTgId) {
        Long longTgId = Long.valueOf(chatTgId);
         List<RegistrationForStudy> studies = registrationForStudyRepo.findAllByIsAppearedFalseAndIsInformTrueAndEmployee_TgId(longTgId);
        System.err.println(studies);
        Comparator<RegistrationForStudy> idComparator = Comparator.comparing(RegistrationForStudy::getId);
        //получение всех записей подходящих по условиям и сортировка по айди чтобы взять последнюю запись и она была самой актуальной
        Collections.sort(studies, idComparator);
        RegistrationForStudy study = studies.get(studies.size()-1);

        study.setAppeared(true);

        registrationForStudyRepo.save(study);

    }
}
