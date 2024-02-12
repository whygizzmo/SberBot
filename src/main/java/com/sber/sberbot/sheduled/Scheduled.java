package com.sber.sberbot.sheduled;

import com.sber.sberbot.services.BotService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class Scheduled {
    private BotService botService;

    public Scheduled(BotService botService) {
        this.botService = botService;
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 13 * * *")//12 часов каждого дня
    public void getQueueSize() {
        System.out.println("Шедулер начал работать: " + LocalDateTime.now());
        botService.sendStudyQuiz();
    }

}
