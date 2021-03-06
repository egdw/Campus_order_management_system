package com.hongdeyan.view;

import com.alibaba.fastjson.JSON;
import com.hongdeyan.constant.Keys;
import com.hongdeyan.constant.RequestStatus;
import com.hongdeyan.constant.RespondStatus;
import com.hongdeyan.message_model.Request;
import com.hongdeyan.message_model.Respond;
import com.hongdeyan.model.Greens;
import com.hongdeyan.model.Order;
import com.hongdeyan.server.NioClient;
import com.hongdeyan.service.GreensService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 * @author hdy
 */
public class ClientView extends javax.swing.JFrame {

    public ClientView() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        GreensTable = new javax.swing.JTable();
        addShop = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        shopTable = new javax.swing.JTable();
        buyButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);


        GreensService greensService = GreensService.getInstance();
        List<Greens> greens = greensService.findAll();
        Object[][] objs = new Object[greens.size()][4];
        for (int i = 0; i < greens.size(); i++) {
            Greens o = (Greens) greens.get(i);
            objs[i][0] = o.getName();
            objs[i][1] = o.getDesc();
            objs[i][2] = o.getMoney();
        }


        DefaultTableModel greensModel = new DefaultTableModel(
                objs,
                new String[]{
                        "菜名", "描述", "价格"
                }
        ) {
            Class[] types = new Class[]{
                    String.class, String.class, Double.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };


        GreensTable.setModel(greensModel);
        jScrollPane2.setViewportView(GreensTable);

        addShop.setText("加入购物车");


        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                                .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addShop)
                                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addShop))
        );

        jTabbedPane1.addTab("菜品", jPanel1);


        DefaultTableModel shoptableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "菜名", "单价", "数量", "总价"
                }
        ) {
            Class[] types = new Class[]{
                    String.class, Double.class, Integer.class, Double.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        addShop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //获取当前选中的
                int selectedRow = GreensTable.getSelectedRow();
                String name = (String) GreensTable.getValueAt(selectedRow, 0);
                String desc = (String) GreensTable.getValueAt(selectedRow, 1);
                Double price = (Double) GreensTable.getValueAt(selectedRow, 2);
                if (selectedRow != -1) {
                    Vector vector = shoptableModel.getDataVector();
                    System.out.println(vector);
                    boolean flag = true;
                    for (int i = 0; i < vector.size(); i++) {
                        Vector v = (Vector) vector.get(i);
                        if (v.get(0).equals(name)) {
                            //说明已经添加了.那么
                            int size = (int) v.get(2);
                            flag = false;
                            shoptableModel.setValueAt((size + 1), i, 2);
                            shoptableModel.setValueAt((size + 1) * price, i, 3);
                            break;
                        }
                    }
                    if (flag) {
                        shoptableModel.addRow(new Object[]{name, price, 1, (price)});
                    }
                    shoptableModel.fireTableDataChanged();
                } else {
                    //说明什么都没有选中..
                }

            }
        });


        this.shopTable.setModel(shoptableModel);
        jScrollPane1.setViewportView(this.shopTable);

        buyButton.setText("下单");

        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String human = (String) JOptionPane.showInputDialog(null, "请输入收货人：\n", "收货人", JOptionPane.PLAIN_MESSAGE, null, null, "收货人");
                String phone = (String) JOptionPane.showInputDialog(null, "请输入手机号：\n", "手机号", JOptionPane.PLAIN_MESSAGE, null, null, "手机号");
                String address = (String) JOptionPane.showInputDialog(null, "请输入收货地址：\n", "收货地址", JOptionPane.PLAIN_MESSAGE, null, null, "收货地址");


                //下单操作.获取所有的数据.提交到服务器
                Vector vector = shoptableModel.getDataVector();

                if (vector.size() == 0) {
                    return;
                }

                double sum = 0;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < vector.size(); i++) {
                    Vector v = (Vector) vector.get(i);
                    sb.append(v.get(0)).append("*").append(v.get(2)).append(",");
                    Double price = (Double) v.get(3);
                    sum += price;
                }


                NioClient client = NioClient.getInstance();
                Request request = new Request();
                request.setCode(RequestStatus.ADD_ORDERS.getCode());
                Order order = new Order();
                order.setBuys(sb.toString());
                order.setBuyer(human);
                order.setPhone(phone);
                order.setAddress(address);
                order.setSum(sum);
                request.setMessage(JSON.toJSONString(order));
                client.send(request, new NioClient.SendBack() {
                    @Override
                    public void get(Respond back) {
                        int code = back.getCode();
                        if (code == RespondStatus.QUERY_SUCESS.getCode()){
                            JOptionPane.showMessageDialog(null, "下单成功", "成功", JOptionPane.PLAIN_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(null, "下单失败", "失败", JOptionPane.ERROR_MESSAGE);
                            int rowCount = shoptableModel.getRowCount();
                            for(int i = 0;i<rowCount;i++){
                                shoptableModel.removeRow(i);
                            }
                            shoptableModel.fireTableDataChanged();
                        }
                    }
                });
            }
        });


        jButton2.setText("删除");

        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = shopTable.getSelectedRow();
                shoptableModel.removeRow(selectedRow);
                shoptableModel.fireTableDataChanged();
            }
        });


        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buyButton)
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buyButton)
                                        .addComponent(jButton2))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("购物车", jPanel2);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        jLabel1.setText("点餐平台");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jTabbedPane1))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addGap(228, 228, 228))
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


    // Variables declaration - do not modify
    private javax.swing.JTable GreensTable;
    private javax.swing.JButton addShop;
    private javax.swing.JButton buyButton;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable shopTable;
    // End of variables declaration
}
