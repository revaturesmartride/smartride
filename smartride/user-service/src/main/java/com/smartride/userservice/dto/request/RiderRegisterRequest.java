package com.smartride.userservice.dto.request;

import lombok.Data;


@Data
public class RiderRegisterRequest {
    private String userName;
    private String userEmail;
    private String userPassword;
    private String userPhone;
}