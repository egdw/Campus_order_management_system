package com.hongdeyan;

import com.hongdeyan.utils.RequestUtils;
import com.hongdeyan.view.LoginView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 校园订餐管理系统
 * 启动类
 *
 * @author hdy
 */
@Slf4j
public class Start {


    public static SocketChannel socketChannel = null;
    public static ServerSocketChannel serverSocketChannel = null;
    public static Selector socketSelector = null;
    public static Selector socketServerSelector = null;

    public static void main(String[] args) {

        HashMap<String, String> map = new HashMap<>();
        map.put("userName","hdy");
        RequestUtils.get(map);

        //判断启动的时候传入的数据.判断是启动服务端还是客户端
        //实际项目应该进行分开.这里为了方便起见直接整合在一个jar文件当中.
//        args = new String[]{"server"};
//        if (args != null) {
////            String input = args[0];
//            if (true) {
//                //如果输入的是server的话代表使用服务器的启动形式
//                startServer();
//            } else {
//                //如果输入的是client或者不输入的话就代表使用客户端的启动形式
//                startClient();
//            }
//        }
    }


    /**
     * 通过NIO技术进行非阻塞的IO处理
     */
    public static void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            //设置成为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //绑定相应的端口
            serverSocketChannel.bind(new InetSocketAddress(8888));
            //获取一个Selector选择器
            socketServerSelector = Selector.open();
            //设置当前的为acept状态
            serverSocketChannel.register(socketServerSelector, SelectionKey.OP_ACCEPT);
            log.info("服务器已经成功启动...");
            boolean isRun = true;
            while (isRun) {
                //循环接收用户的请求
                socketServerSelector.select();
                Iterator<SelectionKey> iterator = socketServerSelector.selectedKeys().iterator();
                //遍历所有的SelectionKey
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    //移除当前的selectionKey.防止重复使用
                    iterator.remove();

                    //状态为accept的时候
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        SocketAddress remoteAddress =
                                socketChannel.getRemoteAddress();
                        log.info("接收到用户的请求.." + remoteAddress + "\n");
                        socketChannel.register(socketServerSelector, SelectionKey.OP_READ);
                    }

                    //状态为read的时候
                    if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(2048);
                        int read = -1;
                        while ((read = channel.read(buffer)) > 0) {
                            buffer.flip();
                            byte[] array = buffer.array();
                            System.out.print(new java.lang.String(array));
                            buffer.clear();
                        }
                        System.out.println();
//                        buffer.flip();
                        channel.shutdownInput();
                        //准备返回Respond
                        channel.register(socketServerSelector, SelectionKey.OP_WRITE);
                    }

                    //状态为可写的时候
                    if (selectionKey.isWritable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
                        byteBuffer.put("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        byteBuffer.flip();
                        channel.write(byteBuffer);
                        byteBuffer.clear();
                        byteBuffer.put("<html><h1>Hello My Server.If you see this page that means my website server is working!</h1></html>".getBytes());
                        byteBuffer.flip();
                        channel.write(byteBuffer);
                        channel.shutdownOutput();
                        selectionKey.cancel();
                        channel.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startClient() {
        //引入Swing的UI库.不适用原始的UI
        try {
//            UIManager.put("RootPane.setupButtonVisible", false);
//            BeautyEyeLNFHelper.translucencyAtFrameInactive = false;
//            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //设置辅助关闭
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginView().setVisible(true);
            }
        });
        try {
            socketChannel = SocketChannel.open();
            //设置为非阻塞
            socketChannel.configureBlocking(false);
            //绑定ip地址段
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
            Selector selector = Selector.open();
            //设置连接状态
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            boolean isRun = true;
            while (isRun) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isConnectable()) {
                        log.info("客户端已经连接");
                    }
                    if (selectionKey.isWritable()) {
                        log.info("客户端已经可以写了");
                        socketChannel.write(ByteBuffer.wrap("你好".getBytes()));
                    }
                    if (selectionKey.isReadable()) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 结束之后释放资源
     */
    public static void finish() {

    }
}
