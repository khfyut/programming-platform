package com.programming.vo;

import lombok.Data;

@Data
public class ChangePasswordVO {
    private String oldPassword;
    private String newPassword;
}
