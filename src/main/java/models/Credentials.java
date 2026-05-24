/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;


public class Credentials {

    private String username;
    private String password;
    private boolean loginStatus;

    public Credentials(
            String username,
            String password,
            boolean loginStatus) {

        this.username = username;
        this.password = password;
        this.loginStatus = loginStatus;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean getLoginStatus() {
        return loginStatus;
    }
}
