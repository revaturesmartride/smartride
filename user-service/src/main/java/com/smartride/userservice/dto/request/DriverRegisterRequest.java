package com.smartride.userservice.dto.request;

import lombok.Data;

@Data
public class DriverRegisterRequest {
    private String userName;
    private String userEmail;
    private String userPassword;
    private String userPhone;

    private String licenceNumber;
    private Long vehicleId;
}
