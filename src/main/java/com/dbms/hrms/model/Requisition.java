package com.dbms.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requisition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Requisition {
    @Id
    @Column(name = "r_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "h_id")
    private Integer createdBy;

    @Column(name = "creation_time")
    private LocalDateTime createdAt;
}
