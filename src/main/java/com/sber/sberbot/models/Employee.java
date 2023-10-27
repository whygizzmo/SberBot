package com.sber.sberbot.models;


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
    @GeneratedValue
    Long id;
    Long tgId;
    String username;
    String phoneNumber;
    boolean isActive;
}
