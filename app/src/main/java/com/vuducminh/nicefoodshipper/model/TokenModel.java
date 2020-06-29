package com.vuducminh.nicefoodshipper.model;


//Token, giúp xác nhận thi nhận, gửi thông báo từ các app server
public class TokenModel {
    private String phone,token;
    private boolean serverToken,shipperToken;

    public TokenModel() {
    }

    public TokenModel(String phone, String token, boolean serverToken, boolean shipperToken) {
        this.phone = phone;
        this.token = token;
        this.serverToken = serverToken;
        this.shipperToken = shipperToken;
    }

    public boolean isServerToken() {
        return serverToken;
    }

    public void setServerToken(boolean serverToken) {
        this.serverToken = serverToken;
    }

    public boolean isShipperToken() {
        return shipperToken;
    }

    public void setShipperToken(boolean shipperToken) {
        this.shipperToken = shipperToken;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
