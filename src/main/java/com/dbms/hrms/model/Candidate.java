package com.dbms.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @Column(name = "c_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id")
    private Requisition requisition;

    private String name;
    private String email;
    private String phone;

    @Column(name = "resume_url")
    private String resumeUrl;

    private String status;

    @Column(name = "creation_time")
    private LocalDateTime appliedAt;
}
