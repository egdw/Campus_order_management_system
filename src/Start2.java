import com.hongdeyan.message_model.Request;
import com.hongdeyan.server.NioClient;

import java.io.IOException;
import java.util.Scanner;

public class Start2 {
    public static void main(String[] args) {
        NioClient nioClient = NioClient.getInstance();
        nioClient.run();
        while (!nioClient.isComplete()) {
        }
        Request request = new Request();
        request.setCode(100);
        request.setMessage("sdadasd");
        request.setUuid("123123");
        System.out.println("连接成功");
        nioClient.send(request, back -> System.out.println("获取到数据为:" + back));
        request.setCode(200);
        nioClient.send(request, back -> System.out.println("获取到数据为:" + back));
        request.setCode(2330);
        nioClient.send(request, back -> System.out.println("获取到数据为:" + back));
    }
}
