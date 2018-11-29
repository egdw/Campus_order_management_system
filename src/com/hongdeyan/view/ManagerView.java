/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hongdeyan.view;

import com.alibaba.fastjson.JSON;
import com.hongdeyan.constant.RequestStatus;
import com.hongdeyan.constant.RespondStatus;
import com.hongdeyan.message_model.Request;
import com.hongdeyan.message_model.Respond;
import com.hongdeyan.model.Duty;
import com.hongdeyan.model.Greens;
import com.hongdeyan.model.Order;
import com.hongdeyan.model.User;
import com.hongdeyan.server.NioClient;
import com.hongdeyan.server.NioServer;
import com.hongdeyan.service.GreensService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author hdy
 */
@Slf4j
public class ManagerView extends javax.swing.JFrame {

    public ManagerView() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        orderTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        greensTable = new javax.swing.JTable();
        addGreensButton = new javax.swing.JButton();
        delGreensButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        userTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);


        DefaultTableModel orderTableModel = new DefaultTableModel(
                new Object[][]{
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null}
                },
                new String[]{
                        "编号", "购买者", "收货地址", "手机", "商品", "烹饪", "派送"
                }
        ) {
            Class[] types = new Class[]{
                    String.class, String.class, String.class, String.class, String.class, Boolean.class, Boolean.class
            };

            boolean[] canEdit = new boolean[]{
                    false, false, true, true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };


        orderTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // 获取所选数据的行数
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 0) {
                    //id不能被修改
                    return;
                }
                String id = (String) orderTable.getValueAt(row, 0);
                String buyer = (String) orderTable.getValueAt(row, 1);
                String address = (String) orderTable.getValueAt(row, 2);
                String phone = (String) orderTable.getValueAt(row, 3);
                String goods = (String) orderTable.getValueAt(row, 4);
                Boolean cooking = (Boolean) orderTable.getValueAt(row, 5);
                Boolean delivery = (Boolean) orderTable.getValueAt(row, 6);

                Order order = new Order();
                order.setId(id);
                order.setAddress(address);
                order.setBuyer(buyer);
                order.setCook(cooking);
                order.setSend(delivery);

                Request request = new Request();
                request.setCode(RequestStatus.UPDATE_ORDERS.getCode());
                request.setMessage(JSON.toJSONString(order));
                NioClient.getInstance().send(request, new NioClient.SendBack() {
                    @Override
                    public void get(Respond back) {
                        if (back.getCode() == RespondStatus.QUERY_SUCESS.getCode()) {
                            log.info("修改成功");
                        } else {
                            log.info("修改失败");
                        }
                    }
                });

                log.info(cooking + "");
                log.info(delivery + "");
            }
        });

        orderTable.setModel(orderTableModel);


        jScrollPane1.setViewportView(orderTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("订单管理", jPanel1);

        GreensService greensService = GreensService.getInstance();
        List<Greens> greens = greensService.findAll();
        Object[][] objs = new Object[greens.size()][4];
        for (int i = 0; i < greens.size(); i++) {
            Greens o = (Greens) greens.get(i);
            objs[i][0] = o.getId();
            objs[i][1] = o.getName();
            objs[i][2] = o.getDesc();
            objs[i][3] = o.getMoney();
        }
        NioClient.getInstance().send(new Request(RequestStatus.FIND_ALL_GREENS.getCode()), new NioClient.SendBack() {
            @Override
            public void get(Respond back) {
                log.info("接受到的:" + back);
            }
        });


        DefaultTableModel greensModel = new DefaultTableModel(
                objs,
                new String[]{
                        "id", "菜名", "描述", "价格"
                }
        ) {
            Class[] types = new Class[]{
                    String.class, String.class, String.class, Double.class
            };

            boolean[] canEdit = new boolean[]{
                    false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        greensModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                //菜品发生变化
                // 获取所选数据的行数
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 0) {
                    //id不能被修改
                    return;
                }
                String id = (String) greensModel.getValueAt(row, 0);
                String name = (String) greensModel.getValueAt(row, 1);
                String desc = (String) greensModel.getValueAt(row, 2);
                Double price = (Double) greensModel.getValueAt(row, 3);
                GreensService greensService = GreensService.getInstance();
                Greens g = greensService.get(id);
                if (g != null) {
                    g.setName(name);
                    g.setDesc(desc);
                    g.setMoney(price);
                    int update = greensService.update(g);
                }
            }
        });
        greensTable.setModel(greensModel);
        jScrollPane2.setViewportView(greensTable);


        addGreensButton.setText("添加");

        addGreensButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //添加菜名
                //这里有问题...
                String greenName = (String) JOptionPane.showInputDialog(null, "请输入添加的菜名：\n", "添加菜名", JOptionPane.PLAIN_MESSAGE, null, null, "请输入菜名");
                String greenDesc = (String) JOptionPane.showInputDialog(null, "请输入菜品的描述：\n", "添加描述", JOptionPane.PLAIN_MESSAGE, null, null, "请输入描述");
                String greenPrice = (String) JOptionPane.showInputDialog(null, "请输入菜品的价格：\n", "添加价格", JOptionPane.PLAIN_MESSAGE, null, null, "请输入价格");
                Greens greens = new Greens(null, greenName, greenDesc, null, Double.parseDouble(greenPrice), 0);
                NioClient instance = NioClient.getInstance();
                Request request = new Request();
                request.setCode(RequestStatus.ADD_GREENS.getCode());
                request.setMessage(JSON.toJSONString(greens));
                instance.send(request, new NioClient.SendBack() {
                    @Override
                    public void get(Respond back) {
                        Greens parseObject = JSON.parseObject(back.getMessage(), Greens.class);
                        //这里要刷新所有的数据
                        //或者添加到最后
                        DefaultTableModel tableModel = (DefaultTableModel)
                                greensTable.getModel();
                        tableModel.addRow(new Object[]{parseObject.getId(),parseObject.getName(),parseObject.getDesc(),parseObject.getMoney()});
                        tableModel.fireTableDataChanged();
                    }
                });
            }
        });

        delGreensButton.setText("删除");

        delGreensButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //删除
                int selectedRow = greensTable.getSelectedRow();
                String id = (String) greensTable.getValueAt(selectedRow, 0);
                NioClient instance = NioClient.getInstance();
                Request request = new Request();
                request.setCode(RequestStatus.REMOVE_GREENS.getCode());
                request.setMessage(id);
                instance.send(request, new NioClient.SendBack() {
                    @Override
                    public void get(Respond back) {
                        String message = back.getMessage();
                        Integer deleteNum = Integer.valueOf(message);
                        if (deleteNum > 0) {
                            //删除成功
                            JOptionPane.showMessageDialog(null, "删除成功", "删除成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            //删除失败
                            JOptionPane.showMessageDialog(null, "删除失败", "删除失败", JOptionPane.ERROR_MESSAGE);
                        }
                        DefaultTableModel tableModel = (DefaultTableModel)
                                greensTable.getModel();
                        tableModel.removeRow(selectedRow);
                        tableModel.fireTableDataChanged();
                    }
                });
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(addGreensButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(delGreensButton)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(addGreensButton)
                                        .addComponent(delGreensButton)))
        );

        jTabbedPane1.addTab("菜品管理", jPanel2);


        NioClient client = NioClient.getInstance();
        Request request = new Request();
        request.setCode(RequestStatus.FIND_ALL_USER.getCode());
        client.send(request, new NioClient.SendBack() {
            @Override
            public void get(Respond back) {
                List<User> list = JSON.parseArray(back.getMessage(), User.class);
                Object[][] obj = new Object[list.size()][4];
                for (int i = 0; i < list.size(); i++) {
                    User user = list.get(i);
                    obj[i][0] = user.getId();
                    obj[i][1] = user.getUsername();
                    obj[i][2] = user.getPassword();
                    if (user.getDuty() != null) {
                        obj[i][3] = user.getDuty().getDutyName();
                    } else {
                        obj[i][3] = null;
                    }
                }

                DefaultTableModel tableModel = new DefaultTableModel(
                        obj,
                        new String[]{
                                "id", "用户名", "密码", "职务"
                        }
                ) {
                    Class[] types = new Class[]{
                            String.class, String.class, String.class, Object.class
                    };

                    boolean[] canEdit = new boolean[]{
                            false, false, true, false
                    };


                    public Class getColumnClass(int columnIndex) {
                        return types[columnIndex];
                    }
                };
                userTable.setModel(tableModel);
                tableModel.addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        // 获取所选数据的行数
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        if (column == 0) {
                            //id不能被修改
                            return;
                        }
                        String id = (String) userTable.getValueAt(row, 0);
                        String username = (String) userTable.getValueAt(row, 1);
                        String password = (String) userTable.getValueAt(row, 2);
                        String duty = (String) userTable.getValueAt(row, 3);


                        Request request1 = new Request();
                        request1.setCode(RequestStatus.UPDATE_USER.getCode());
                        User user = new User();
                        Duty duty1 = new Duty();
                        duty1.setDutyName(duty);
                        user.setDuty(duty1);
                        user.setPassword(password);
                        user.setUsername(username);
                        user.setId(id);
                        request1.setMessage(JSON.toJSONString(user));
                        NioClient.getInstance().send(request1, new NioClient.SendBack() {
                            @Override
                            public void get(Respond back) {
                                System.out.println(back);
                            }
                        });


                        System.out.println(row);
                        System.out.println(userTable.getValueAt(row, column));
                    }
                });
                jScrollPane3.setViewportView(userTable);
            }
        });


        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("用户管理", jPanel3);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        jLabel1.setText("后台管理系统");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jTabbedPane1))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(207, 207, 207)
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        pack();
    }


    private void delGreensButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private javax.swing.JTable greensTable;
    private javax.swing.JButton addGreensButton;
    private javax.swing.JButton delGreensButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable orderTable;
    private javax.swing.JTable userTable;
}
