package com.smartride.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="drivers_db")
@Builder
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String licenceNumber;

    private  String vehicleId;

    private Double rating;

    private int totalRides;

    @Enumerated(EnumType.STRING)
    private DriverAvailabilityStatus availabilityStatus;

    @Enumerated(EnumType.STRING)
    private DriverApprovalStatus approvalStatus;

    @CreationTimestamp
    private Timestamp createdAt;

}
