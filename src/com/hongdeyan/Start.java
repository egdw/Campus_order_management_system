package com.hongdeyan;

import com.hongdeyan.server.NioServer;
import com.hongdeyan.view.LoginView;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 校园订餐管理系统
 * 启动类
 *
 * @author hdy
 */
@Slf4j
public class Start {

    public static void main(String[] args) {


        Logger logger = Logger.getLogger(String.valueOf(Start.class));
        logger.setLevel(Level.INFO);

        //判断启动的时候传入的数据.判断是启动服务端还是客户端
        //实际项目应该进行分开.这里为了方便起见直接整合在一个jar文件当中.
        args = new String[]{"123"};
        if (args != null) {
//            String input = args[0];
            if (false) {
                NioServer server = NioServer.getInstance();
            } else {
                //如果输入的是client或者不输入的话就代表使用客户端的启动形式
                try {
//                    UIManager.put("RootPane.setupButtonVisible", false);
//                    BeautyEyeLNFHelper.translucencyAtFrameInactive = false;
//                    org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
                    //设置辅助关闭
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            new LoginView().setVisible(true);
                        }
                    });
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

    }
}
