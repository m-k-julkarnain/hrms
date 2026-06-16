package com.dbms.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @Column(name = "p_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;


    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    private String method;

    private String remarks;

    @Column(name = "h_id")
    private Integer paidBy;

    @Column(name = "creation_time")
    private LocalDateTime createdAt;
}
