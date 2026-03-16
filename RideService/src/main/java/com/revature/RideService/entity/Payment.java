package com.revature.RideService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
}
