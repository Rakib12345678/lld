package model;

public class User {
    String id;
    String name;
    String phoneNo;
    String email;
    String fcmToken;
    String apnToken;

    public User(String id, String name, String phoneNo, String email, String fcmToken, String apnToken) {
        this.id = id;
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
        this.fcmToken = fcmToken;
        this.apnToken = apnToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getApnToken() {
        return apnToken;
    }

    public void setApnToken(String apnToken) {
        this.apnToken = apnToken;
    }
}
