package com.dbms.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @Column(name = "a_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    // matches your DB enum: 'present','absent','late'
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "h_id")
    private Integer recordedBy;

    @Column(name = "record_time")
    private LocalDateTime recordedAt;
}
