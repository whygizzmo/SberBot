package com.sber.sberbot.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)

@Table(name = "tb_bot_messages")
public class MessageFromBot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String messageText;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;
    LocalDateTime messageDate;
}
