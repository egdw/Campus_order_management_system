import com.hongdeyan.message_model.Request;
import com.hongdeyan.server.NioClient;

import java.io.IOException;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Start2 {
    public static void main(String[] args) throws InterruptedException {
        NioClient nioClient = NioClient.getInstance();
        Request request = new Request();
        request.setCode(100);
        nioClient.send(request, new NioClient.SendBack() {
            @Override
            public void get(String back) {
                System.out.println("返回的数据为:" + back);
            }
        });
        TimeUnit.SECONDS.sleep(2);

        request.setCode(200);
        nioClient.send(request, new NioClient.SendBack() {
            @Override
            public void get(String back) {
                System.out.println("返回的数据为:" + back);
            }
        });
        TimeUnit.SECONDS.sleep(2);

        request.setCode(300);
        nioClient.send(request, new NioClient.SendBack() {
            @Override
            public void get(String back) {
                System.out.println("返回的数据为:" + back);
            }
        });
        TimeUnit.SECONDS.sleep(2);

        request.setCode(400);
        nioClient.send(request, new NioClient.SendBack() {
            @Override
            public void get(String back) {
                System.out.println("返回的数据为:" + back);
            }
        });

    }
}
