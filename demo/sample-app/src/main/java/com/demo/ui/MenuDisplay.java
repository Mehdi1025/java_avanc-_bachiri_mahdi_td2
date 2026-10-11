package com.demo.ui;

import java.util.ResourceBundle;

public final class MenuDisplay {

    public void showTitle() {
        System.out.println(ResourceBundle.getBundle("messages").getString("msg_5"));
    }

    public void showOptionCount(int count) {
        System.out.println(ResourceBundle.getBundle("messages").getString("msg_6") + count);
    }
}
