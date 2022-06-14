package com.xiaowei.chatguide;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class ChatGuideClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatGuideClient.class.getName());

    private final ChatGuideGrpc.ChatGuideStub asyncStub;


    public ChatGuideClient(Channel channel) {
        asyncStub = ChatGuideGrpc.newStub(channel);
    }

    public void chat(String msg, int user, int count) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<ChatResponse> responseStreamObserver = new StreamObserver<ChatResponse>() {
            @Override
            public void onNext(ChatResponse chatResponse) {
                logger.info("onNext-服务端响应: {}, 用户Id {} :", chatResponse.getMsg(), chatResponse.getUserId());

            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
                logger.warn("send failed {}", Status.fromThrowable(t));
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
                logger.info("Finished send");

            }
        };

        StreamObserver<ChatRequest> requestStreamObserver = asyncStub.chat(responseStreamObserver);
        for(int i =0;i<count;i++){
            ChatRequest chatRequest = ChatRequest.newBuilder()
                    .setUserId(user)
                    .setMsg("这是一条来自客户端的消息: 你好，" + user + new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS").format(new Date()))
                    .build();
            requestStreamObserver.onNext(chatRequest);
        }

        // 客户端告诉服务端：数据已经发完了
        requestStreamObserver.onCompleted();
        finishLatch.await();
        logger.info("service finish");
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:50051").usePlaintext().build();

        try {
            ChatGuideClient client = new ChatGuideClient(channel);
            client.chat("Hello world",1001,10);
        }
        finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

        }

    }


}
