package com.sber.sberbot.models.dtos;


import com.sber.sberbot.models.MessageFromBot;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindEmployeeDto {
    Long chatId;
    String username;

}
