package com.hongdeyan.message_model;

import com.sun.deploy.net.HttpResponse;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class HttpRespond {
    private static final Pattern statusPattern = Pattern.compile("\\A(HTTP/1\\.[01]+) ([0-9]+) (\\S+?)\\r?\\n");
    private static final Pattern headerPattern = Pattern.compile("\\A([a-zA-Z0-9_-]+)[ ]*:[ ]*([^\\r\\n]+?)\\r?\\n", Pattern.UNIX_LINES);

    //表示当前的协议
    private String protocol;
    private int status;
    private String message;
    private HttpHeaders headers;


    public HttpRespond() {
        this.headers = new HttpHeaders();
    }
}
