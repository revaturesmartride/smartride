package com.smartride.userservice.mapper;

import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.model.DriverApprovalStatus;
import com.smartride.userservice.model.DriverAvailabilityStatus;
import com.smartride.userservice.model.DriverProfile;
import com.smartride.userservice.model.UserEntity;

public class DriverMapper {
    public static DriverProfile toEntity(DriverRegisterRequest request, UserEntity user) {
        return DriverProfile.builder()
                .user(user)
                .licenceNumber(request.getLicenceNumber())
                .vehicleId(request.getVehicleId())
                .rating(0.0)
                .totalRides(0)
                .availabilityStatus(DriverAvailabilityStatus.OFFLINE)
                .approvalStatus(DriverApprovalStatus.PENDING)
                .build();
    }
}
