package com.xiaowei.chatguide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactorChatGuideSever {
    private static final Logger logger = LoggerFactory.getLogger(ReactorChatGuideSever.class.getName());


    private static class ChatGuideService extends ReactorChatGuideGrpc.ChatGuideImplBase {

        @Override
        public Flux<ChatResponse> chat(Flux<ChatRequest> request) {
            return request.map(chatRequest -> {
                String msg = chatRequest.getMsg();
                logger.info("客户端说: [" + msg + "]");
                int userId = chatRequest.getUserId();
                return ChatResponse.newBuilder()
                        .setUserId(chatRequest.getUserId())
                        .setMsg("这是一条来自[服务端]的消息: 你好，收到了-" + userId + " 的消息. " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS").format(new Date()) + "\n")
                        .build();
            });

        }
    }


}
