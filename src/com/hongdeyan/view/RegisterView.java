package com.hongdeyan.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterView extends JFrame implements ViewParent {

    @Override
    public void init() {
        this.setVisible(true);
        this.setSize(500, 500);
        this.setTitle("登录或注册");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Container container = this.getContentPane();
        container.setLayout(null);


        JLabel username_hint = new JLabel("用户名:");
        username_hint.setBounds(10,30,80,40);
        JTextField username = new JTextField();
        username.setBounds(100, 30, 100, 40);

        JLabel password_hint = new JLabel("用户名:");
        password_hint.setBounds(10,80,80,40);
        JTextField password = new JTextField();
        password.setBounds(100, 80, 100, 40);


        JLabel code_hint = new JLabel("用户名:");
        code_hint.setBounds(10,150,80,40);
        JTextField code = new JTextField();
        password.setBounds(100, 150, 100, 40);
        //登录按钮
        JButton jb = new JButton("登录");        // 创建按钮
        jb.setBounds(200, 350, 100, 50);        // 按钮位置及大小
        jb.addActionListener(new ActionListener() {        // 监听器，用于监听点击事件
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterView().init();
            }
        });


        container.add(jb);
        container.add(username);
        container.add(username_hint);
        container.add(password_hint);
        container.add(code_hint);
        container.add(password);
        container.add(code);
    }


}
