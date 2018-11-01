package com.hongdeyan.server;

import com.alibaba.fastjson.JSON;
import com.hongdeyan.message_model.Request;
import com.hongdeyan.static_class.RSA;
import com.hongdeyan.utils.RsaUtil;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class NioServer {


    private static ServerSocketChannel serverSocketChannel = null;
    private static Selector socketServerSelector = null;

    private static NioServer nioServer;
    private static Object lock = new Object();

    private NioServer() {
        Logger logger = Logger.getLogger(String.valueOf(NioServer.class));
        logger.setLevel(Level.INFO);
        startServer();
    }

    public synchronized static NioServer getInstance() {
        synchronized (lock) {
            if (nioServer == null) {
                nioServer = new NioServer();
            }
            return nioServer;
        }
    }

    /**
     * 通过NIO技术进行非阻塞的IO处理
     */
    private static void startServer() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("服务器已经成功启动...");
        boolean isRun = true;
        while (isRun) {
            //循环接收用户的请求
            try {
                int select = socketServerSelector.select();
                if (select <= 0) {
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Iterator<SelectionKey> iterator = socketServerSelector.selectedKeys().iterator();
            //遍历所有的SelectionKey
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //移除当前的selectionKey.防止重复使用
                iterator.remove();
                //状态为accept的时候
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = null;
                    try {
                        socketChannel = serverSocketChannel.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socketChannel.configureBlocking(false);
                        SocketAddress remoteAddress =
                                socketChannel.getRemoteAddress();
                        log.info("接收到用户的请求.." + remoteAddress + "\n");
                        socketChannel.register(socketServerSelector, SelectionKey.OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                        selectionKey.interestOps(SelectionKey.OP_READ);
                }
                //状态为read的时候
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(2048);
                    int read = -1;
                    StringBuilder sb = new StringBuilder();
                    try {
                        while ((read = channel.read(buffer)) > 0) {
                            buffer.flip();
                            byte[] array = buffer.array();
                            sb.append(new String(array, 0, buffer.limit()));
                            buffer.clear();
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    String message = sb.toString();
                    if ("".equals(message) || message == null) {
                        //说明客户端已断开
                        selectionKey.cancel();
                        try {
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //说明客户端没有断开
//                        System.out.println("服务器获取到的信息为:" + message);
                        //准备返回Respond
                        try {
                            log.info(message);
                            String decryptData = RsaUtil.decryptData(message, RSA.PRIVATEKEY);
                            selectionKey.attach(JSON.parseObject(decryptData, Request.class));
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                        } catch (Exception e) {
                            log.info("客户端断开.." + e.getMessage());
                        }
                    }
                }

                //状态为可写的时候
                if (selectionKey.isValid() && selectionKey.isWritable()) {
                    Request attachment = (Request) selectionKey.attachment();
                    if (attachment == null) {
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        return;
                    }
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
                    System.out.println(attachment);
                    byteBuffer.put((attachment.getCode() + "").getBytes());
                    log.info("返回数据:" + attachment.getCode());
                    byteBuffer.flip();
                    try {
                        channel.write(byteBuffer);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    selectionKey.attach(null);
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }

            }
        }
    }
}
