package com.hongdeyan.server;

import com.alibaba.fastjson.JSON;
import com.hongdeyan.constant.RequestStatus;
import com.hongdeyan.constant.RespondStatus;
import com.hongdeyan.message_model.Request;
import com.hongdeyan.message_model.Respond;
import com.hongdeyan.model.Duty;
import com.hongdeyan.model.Greens;
import com.hongdeyan.model.Order;
import com.hongdeyan.model.User;
import com.hongdeyan.service.DutyService;
import com.hongdeyan.service.GreensService;
import com.hongdeyan.service.OrderService;
import com.hongdeyan.service.UserService;
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
import java.util.List;
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
//                            String decryptData = RsaUtil.decryptData(message, RSA.PRIVATEKEY);
                            //获取到数据之后进行解密操作
                            selectionKey.attach(JSON.parseObject(message, Request.class));
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
                    //这里需要进行数据的操作

                    Respond respond = handler(attachment);

                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

                    byteBuffer.put((JSON.toJSONString(respond)).getBytes());
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


    /**
     * 在这里处理相关的请求
     *
     * @param request
     * @return
     */
    private static Respond handler(Request request) {
        Respond respond = new Respond();
        log.info("当前请求的请求码:"+request.getCode());
        if (RequestStatus.LOGIN.getCode() == request.getCode()) {
            User user = JSON.parseObject(request.getMessage(), User.class);
            log.info(user + "");
            UserService service = UserService.getInstance();
            User byUserNameAndPassword = service.findByUserNameAndPassword(user.getUsername(), user.getPassword());
            log.info("查询到的用户为:" + byUserNameAndPassword);
            if (byUserNameAndPassword != null) {
                respond.setCode(RespondStatus.LOGIN_SUCCESS.getCode());
                respond.setMessage(JSON.toJSONString(byUserNameAndPassword));
            } else {
                respond.setCode(RespondStatus.LOGIN_FAIL.getCode());
                respond.setMessage(RespondStatus.LOGIN_FAIL.getMessage());
            }
            //请求登录
        } else if (RequestStatus.REGISTER.getCode() == request.getCode()) {
            //请求注册
            User user = JSON.parseObject(request.getMessage(), User.class);
            UserService service = UserService.getInstance();
            User byUserNameAndPassword = service.findByUserName(user.getUsername());
            log.info(byUserNameAndPassword + "");
            if (byUserNameAndPassword == null && user != null && !"".equals(user.getUsername()) && !"".equals(user.getPassword())) {
                //如果是等于null.说明还没有这个用户
                Duty duty =
                        user.getDuty();
                if (duty != null) {
                    String dutyName = duty.getDutyName();
                    DutyService dutyService = DutyService.getInstance();
                    Duty dutyByName = dutyService.getDutyByName(dutyName);
                    if (dutyByName != null && dutyByName.getId() != null) {
                        user.setDuty(dutyByName);
                    }
                }
                User add = service.add(user);
                respond.setCode(RespondStatus.REGISTER_SUCCESS.getCode());
                respond.setMessage(JSON.toJSONString(add));
            } else {
                respond.setCode(RespondStatus.REGISTER_FAIL.getCode());
                respond.setMessage("存在重复的用户,无法注册");
            }
        } else if (RequestStatus.FIND_ALL_USER.getCode() == request.getCode()) {
            //查找所有的用户
            UserService service = UserService.getInstance();
            List<User> all = service.findAll();
            respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            respond.setMessage(JSON.toJSONString(all));
        } else if (RequestStatus.UPDATE_USER.getCode() == request.getCode()) {
            //修改用户,这里只能修改用户的密码
            User user = JSON.parseObject(request.getMessage(), User.class);
            int update = UserService.getInstance().update(user);
            if (update>0){
                respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            }
        } else if (RequestStatus.ADD_GREENS.getCode() == request.getCode()) {
            //添加菜品
            GreensService instance = GreensService.getInstance();
            Greens greens = JSON.parseObject(request.getMessage(), Greens.class);

            log.info("获取到请求,添加菜品:" + greens);
            greens = instance.add(greens);
            respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            respond.setMessage(JSON.toJSONString(greens));
        } else if (RequestStatus.ADD_ORDERS.getCode() == request.getCode()) {
            //添加订单
            Order order = JSON.parseObject(request.getMessage(), Order.class);
            //不想打这么复杂了.麻烦死了- -.不校验数据了.又没钱
            OrderService instance = OrderService.getInstance();
            order = instance.add(order);
            respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            respond.setMessage(JSON.toJSONString(order));
        } else if (RequestStatus.REMOVE_GREENS.getCode() == request.getCode()) {
            //删除菜名
            String id = request.getMessage();
            GreensService instance = GreensService.getInstance();
            int remove = instance.remove(id);
            respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            respond.setMessage(remove + "");
        }else if(RequestStatus.FIND_ALL_GREENS.getCode() == request.getCode()){
            //查询所有的菜名
            log.info("查询所有的菜品");
            GreensService greensService = GreensService.getInstance();
            List<Greens> greens = greensService.findAll();
            respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            respond.setMessage(JSON.toJSONString(greens));
        }else if(RequestStatus.UPDATE_ORDERS.getCode() == request.getCode()){
            Order order = JSON.parseObject(request.getMessage(), Order.class);
            Order order1 = OrderService.getInstance().get(order.getId());
            order1.setSend(order.isSend());
            order1.setCook(order.isCook());
            order.setPhone(order.getPhone());
            order.setBuys(order.getBuys());
            int update = OrderService.getInstance().update(order1);
            if(update > 0){
                respond.setCode(RespondStatus.QUERY_SUCESS.getCode());
            }else{
                respond.setCode(RespondStatus.QUERY_FAIL.getCode());
            }
        }
        return respond;
    }
}
