package com.sber.sberbot.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@RequiredArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tb_admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;
    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    Admin admin;
    LocalDate startDate;
    LocalDate endDate;
}
