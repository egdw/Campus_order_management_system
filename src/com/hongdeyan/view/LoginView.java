/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hongdeyan.view;

import com.alibaba.fastjson.JSON;
import com.hongdeyan.constant.Keys;
import com.hongdeyan.constant.RequestStatus;
import com.hongdeyan.constant.RespondStatus;
import com.hongdeyan.message_model.Request;
import com.hongdeyan.message_model.Respond;
import com.hongdeyan.model.User;
import com.hongdeyan.server.NioClient;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author hdy
 */
@Slf4j
public class LoginView extends javax.swing.JFrame {

    public LoginView() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        initComponents();
    }

    private void initComponents() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();//获取屏幕尺寸对象
        Dimension myframe = this.getSize();//获取当前窗体的尺寸对象
        int w = (screen.width / 2 - myframe.width);//水平位置
        int h = (screen.height / 2 - myframe.height);//垂直位置
        setLocation(w, h);
        usernameInput = new javax.swing.JTextField();
        usernamePanel = new javax.swing.JLabel();
        password = new javax.swing.JTextField();
        passwordPanel = new javax.swing.JLabel();
        savePassword = new javax.swing.JCheckBox();
        autoLogin = new javax.swing.JCheckBox();
        loginButton = new javax.swing.JButton();
        registerButton = new javax.swing.JLabel();
        ForgortPassword = new javax.swing.JLabel();
        owner = new javax.swing.JLabel();
        title = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        usernamePanel.setText("用户名:");

        passwordPanel.setText("密码:");
        passwordPanel.setToolTipText("");

        savePassword.setText("记住密码");
        savePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePasswordActionPerformed(evt);
            }
        });

        autoLogin.setText("自动登录");

        loginButton.setText("登录");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (usernameInput.getText().isEmpty() || password.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "请输入完整的数据", "输入错误", JOptionPane.ERROR_MESSAGE);
                } else {
                    //说明可以尝试进行登陆操作发送
                    String username = usernameInput.getText();
                    String _password = password.getText();
                    NioClient client = NioClient.getInstance();
                    User user = new User();
                    user.setPassword(_password);
                    user.setUsername(username);
                    Request request = new Request(RequestStatus.LOGIN.getCode(), JSON.toJSONString(user));
                    client.send(request, new NioClient.SendBack() {
                        @Override
                        public void get(Respond back) {
                            int code = back.getCode();
                            if (RespondStatus.LOGIN_SUCCESS.getCode() == code) {
                                //登录成功
                                JOptionPane.showMessageDialog(null, "登录成功", "登录成功", JOptionPane.INFORMATION_MESSAGE);
                                //那么就销毁当前的窗体.进入新的窗体
                                String message = back.getMessage();
                                Keys.currentName = username;
                                Keys.currentPassword = _password;
                                log.info(message);
                                User object = JSON.parseObject(message, User.class);
                                //获取到登陆的详细信息
                                if ("管理员".equals(object.getDuty().getDutyName())) {
                                    java.awt.EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            new ManagerView().setVisible(true);
                                        }
                                    });

                                } else if ("消费者".equals(object.getDuty().getDutyName())) {
                                    java.awt.EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            new ClientView().setVisible(true);
                                        }
                                    });
                                } else if ("厨师".equals(object.getDuty().getDutyName())) {
                                    java.awt.EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            new ManagerView().setVisible(true);
                                        }
                                    });
                                } else if ("外卖小哥".equals(object.getDuty().getDutyName())) {
                                    java.awt.EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            new ManagerView().setVisible(true);
                                        }
                                    });
                                } else {
                                    java.awt.EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            new ManagerView().setVisible(true);
                                        }
                                    });
                                }
                                LoginView.this.dispose();
                            } else {
                                //登录失败
                                JOptionPane.showMessageDialog(null, "账户或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                }
            }
        });

        registerButton.setText("注册账户");

        registerButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new RegisterView().setVisible(true);
                    }
                });
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        ForgortPassword.setText("忘记密码");

        owner.setText("©洪德衍");

        title.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        title.setText("校园订餐管理系统");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(42, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(owner)
                                                .addGap(175, 175, 175))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(usernamePanel)
                                                        .addComponent(passwordPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(loginButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addComponent(savePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(autoLogin))
                                                        .addComponent(password)
                                                        .addComponent(usernameInput, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(registerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(ForgortPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(16, 16, 16))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(title)
                                                .addGap(100, 100, 100))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(title)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(usernameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(usernamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(registerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(ForgortPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(passwordPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(autoLogin)
                                        .addComponent(savePassword))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(loginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(owner)
                                .addContainerGap())
        );

        pack();
    }

    private void savePasswordActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private javax.swing.JLabel ForgortPassword;
    private javax.swing.JCheckBox autoLogin;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel owner;
    private javax.swing.JTextField password;
    private javax.swing.JLabel passwordPanel;
    private javax.swing.JLabel registerButton;
    private javax.swing.JCheckBox savePassword;
    private javax.swing.JLabel title;
    private javax.swing.JTextField usernameInput;
    private javax.swing.JLabel usernamePanel;
}
