package com.hongdeyan;

import com.hongdeyan.model.User;
import com.hongdeyan.orm.Orm;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 校园订餐管理系统
 * 启动类
 *
 * @author hdy
 */
@Slf4j
public class Start {

    public static void main(String[] args) {
        //判断启动的时候传入的数据.判断是启动服务端还是客户端
        //实际项目应该进行分开.这里为了方便起见直接整合在一个jar文件当中.

        User obj = Orm.get("5bd423916e99786421562c81", User.class);
        System.out.println(obj);
//        args = new String[]{"server"};
//        if (args != null) {
//            String input = args[0];
//            if (input.equals("server")) {
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
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //设置成为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //绑定相应的端口
            serverSocketChannel.bind(new InetSocketAddress(8888));
            //获取一个Selector选择器
            Selector selector = Selector.open();
            //设置当前的为acept状态
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("服务器已经成功启动...");
            boolean isRun = true;
            while (isRun) {
                //循环接收用户的请求
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                //遍历所有的SelectionKey
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    //移除当前的selectionKey.防止重复使用
                    iterator.remove();

                    //状态为accept的时候
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        SocketAddress remoteAddress =
                                socketChannel.getRemoteAddress();
                        log.info("接收到用户的请求.." + remoteAddress);
                    }

                    //状态为read的时候
                    if (selectionKey.isReadable()) {

                    }

                    //状态为可写的时候
                    if (selectionKey.isWritable()) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startClient() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
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
