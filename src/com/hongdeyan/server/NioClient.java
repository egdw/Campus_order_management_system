package com.hongdeyan.server;

import com.hongdeyan.message_model.Request;
import com.mongodb.util.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioClient{
    private SocketChannel socketChannel = null;
    private Selector socketSelector = null;
    private static Object lock = new Object();
    private static NioClient nioClient;
    private volatile boolean isConnect = false;


    private volatile String sendMessage;
    private volatile SendBack sendBack;

    private volatile SelectionKey selectionKey;

    private NioClient() {

    }


    public static NioClient getInstance() {
        synchronized (lock) {
            if (nioClient == null) {
                nioClient = new NioClient();
            }
            return nioClient;
        }
    }

    public void run() {
        init();
        boolean isRun = true;
        while (isRun) {
            int select = 0;
            try {
                select = socketSelector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (select <= 0) continue;
            Iterator<SelectionKey> iterator = socketSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isWritable() && sendMessage != null) {
                    log.info("客户端已经可以写了");
                    try {
                        socketChannel.write(ByteBuffer.wrap(sendMessage.getBytes()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }
                if (selectionKey.isReadable()) {
                    log.info("客户端已经可以读了");
                    ByteBuffer allocate = ByteBuffer.allocate(1024);
                    StringBuilder sb = new StringBuilder();
                    long read = -1;
                    try {
                        while ((read = socketChannel.read(allocate)) > 0) {
                            allocate.flip();
                            sb.append(new String(allocate.array(), 0, (int) read));
                            allocate.clear();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String message = sb.toString();
                    if (message == null || "".equals(message)) {
                        //说明服务器已经断开
                        log.info("连接到服务器断开");
                        selectionKey.cancel();
                        try {
                            socketChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isRun = false;
                    }
                    sendBack.get(message);
//                    一次交互完成
                    synchronized (sendMessage) {
                        sendMessage = null;
                        selectionKey.interestOps(0);
                    }
                }
            }
        }
    }


    public synchronized void send(Request request, SendBack sendBack){
        if (socketChannel == null || socketSelector == null || request == null || sendBack == null) {
            throw new NullPointerException();
        }
        try {
            if (!socketSelector.isOpen() || !socketChannel.finishConnect()) {
                try {
                    throw new IllegalAccessException("selector没有开启");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socketChannel.register(socketSelector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
        if (selectionKey != null && selectionKey.isValid()) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
        this.sendMessage = com.alibaba.fastjson.JSON.toJSONString(request);
        this.sendBack = sendBack;
    }

    /**
     * 初始化数据
     */
    private void init() {
        try {
            socketChannel = SocketChannel.open();
            //设置为非阻塞
            socketChannel.configureBlocking(false);
            //绑定ip地址段
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
            socketSelector = Selector.open();
            //设置连接状态
            socketChannel.register(socketSelector, SelectionKey.OP_CONNECT);
            boolean isRun = true;
            while (isRun) {
                int select = socketSelector.select();
                if (select <= 0) continue;
                Iterator<SelectionKey> iterator = socketSelector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isConnectable()) {
                        log.info("客户端已经连接到服务器完成.");
                        //切换到写的的模式
                        isRun = false;
                        synchronized (NioClient.this) {
                            isConnect = true;
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface SendBack {
        public void get(String back);
    }


    /**
     * 判断客户端是否连接到服务器端成功
     *
     * @return
     */
    public synchronized boolean isComplete() {
        return isConnect;
    }


    public void finish() {
        if (socketSelector != null) {
            Set<SelectionKey> selectionKeys =
                    socketSelector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                next.cancel();
            }
        }
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
