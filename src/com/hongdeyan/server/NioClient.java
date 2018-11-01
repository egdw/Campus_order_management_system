package com.hongdeyan.server;

import com.hongdeyan.message_model.Request;
import com.hongdeyan.static_class.RSA;
import com.hongdeyan.utils.RsaUtil;
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
import java.util.concurrent.TimeUnit;

@Slf4j
public class NioClient {
    private SocketChannel socketChannel = null;
    private Selector socketSelector = null;
    private static Object lock = new Object();
    private static NioClient nioClient;
    private volatile boolean isConnect = false;
    private volatile static Thread mainThread;
    private volatile SelectionKey selectionKey;
    private volatile boolean isRun = false;
    private volatile Boolean sending = false;

    private NioClient() {
        init();
    }


    public static NioClient getInstance() {
        synchronized (lock) {
            if (nioClient == null) {
                nioClient = new NioClient();
            }
            return nioClient;
        }
    }

    private void run() {
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isRun = true;
                while (isRun) {
                    int select = 0;
                    try {
                        select = socketSelector.selectNow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (select <= 0) continue;
                    Iterator<SelectionKey> iterator = socketSelector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isWritable() && selectionKey.isValid()) {
//                            log.info("客户端已经可以写了");
                            try {
                                Object[] attachment = (Object[]) selectionKey.attachment();
                                if (attachment != null) {
                                    String encryptData = RsaUtil.encryptData(((String) attachment[1]), RSA.PUBLICKEY);
                                    socketChannel.write(ByteBuffer.wrap(encryptData.getBytes()));
                                    selectionKey.interestOps(SelectionKey.OP_READ);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (selectionKey.isReadable() && selectionKey.isValid()) {
//                            log.info("客户端已经可以读了");
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
                                return;
                            }
                            SendBack sendBack = null;
                            Object[] attachment = (Object[]) selectionKey.attachment();
                            sendBack = (SendBack) attachment[0];

                            //取消绑定的数据
                            selectionKey.attach(null);
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                            sendBack.get(message);
//                            selectionKey.interestOps(0);
                        }
                    }
                }
            }
        });
        mainThread.setName("client_main_thread");
        mainThread.start();
    }


    public synchronized void send(Request request, SendBack sendBack) {
        if (mainThread == null || mainThread.isInterrupted()) {
            run();
        }
        if (socketChannel == null || socketSelector == null || request == null || sendBack == null) {
            //如果没有,这里先等待一下
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean complete = nioClient.isComplete();
            if (!complete) {
                throw new NullPointerException();
            }
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
        if (selectionKey != null && selectionKey.isValid()) {
            //如果selectionkey存在的话就直接调用selectionKey
            synchronized (selectionKey) {
                selectionKey.attach(new Object[]{sendBack, com.alibaba.fastjson.JSON.toJSONString(request)});
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
        } else {
            try {
                //否则使用channel修改OP
                synchronized (socketChannel) {
                    SelectionKey register = socketChannel.register(socketSelector, SelectionKey.OP_WRITE);
                    register.attach(new Object[]{sendBack, com.alibaba.fastjson.JSON.toJSONString(request)});
                }
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
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
