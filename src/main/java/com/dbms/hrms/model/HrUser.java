package com.dbms.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrUser {
    @Id
    @Column(name = "h_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

}
