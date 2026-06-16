package com.dbms.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @Column(name = "e_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "emp_code", unique = true)
    private String empCode;

    private String name;

    private String email;

    private String phone;

    private String position;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    private BigDecimal salary;

    @Column(name = "creation_time")
    private java.time.LocalDateTime createdAt;

    @Column(name = "h_id")
    private Integer createdBy;
}
