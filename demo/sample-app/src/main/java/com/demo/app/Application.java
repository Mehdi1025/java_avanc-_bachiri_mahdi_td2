package com.demo.app;

import com.demo.service.GreetingService;
import com.demo.ui.MenuDisplay;

public final class Application {

    public static void main(String[] args) {
        MenuDisplay menuDisplay = new MenuDisplay();
        GreetingService greetingService = new GreetingService();

        menuDisplay.showTitle();
        System.out.println(greetingService.welcome("Mehdi"));
        System.out.println(greetingService.goodbye());
    }
}
