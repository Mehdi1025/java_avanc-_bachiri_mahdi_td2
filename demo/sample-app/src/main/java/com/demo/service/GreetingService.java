package com.demo.service;

import java.util.ResourceBundle;

public final class GreetingService {

    public String welcome(String userName) {
        return ResourceBundle.getBundle("messages").getString("msg_2") + userName + ResourceBundle.getBundle("messages").getString("msg_3");
    }

    public String goodbye() {
        return ResourceBundle.getBundle("messages").getString("msg_4");
    }
}
