package com.demo.app;

import com.demo.service.GreetingService;
import com.demo.ui.MenuDisplay;
import java.util.ResourceBundle;

public final class Application {

    public static void main(String[] args) {
        MenuDisplay menuDisplay = new MenuDisplay();
        GreetingService greetingService = new GreetingService();
        menuDisplay.showTitle();
        System.out.println(greetingService.welcome(ResourceBundle.getBundle("messages").getString("msg_1")));
        System.out.println(greetingService.goodbye());
    }
}
