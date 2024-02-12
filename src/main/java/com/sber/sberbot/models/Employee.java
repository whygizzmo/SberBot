package com.sber.sberbot.models;


import com.sber.sberbot.models.enums.State;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tb_employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long tgId;
    String username;
    boolean isActive;
    @Enumerated(value = EnumType.STRING)
    State statusTg;
}
