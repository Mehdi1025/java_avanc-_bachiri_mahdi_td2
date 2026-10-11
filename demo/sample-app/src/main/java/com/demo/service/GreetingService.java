package com.demo.service;

public final class GreetingService {

    public String welcome(String userName) {
        return "Bienvenue " + userName + ", heureux de vous revoir.";
    }

    public String goodbye() {
        return "Au revoir et à bientôt.";
    }
}
