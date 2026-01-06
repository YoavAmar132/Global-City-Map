package common.messages;

import java.io.Serializable;

public class RegisterPayload implements Serializable {
    private  String username;
    private   String password;
    private final String firstname;
    private final String lastname;
    private final String phonenum;
    private final String credit;
    private final String pin;

    public RegisterPayload(String username,String password,String firstname, String lastname,String phonenum,String credit,String pin) {
        this.username=username;
        this.password=password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phonenum = phonenum;
        this.credit = credit;
        this.pin=pin;
    }
    public void setUsername(String username)
    {
        this.username=username;
    }
    public void setPassword(String password)
    {
        this.password=password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhonenum() {
        return phonenum;
    }

    public String getCredit() {
        return credit;
    }

    public String getPin() {
        return pin;
    }
}
