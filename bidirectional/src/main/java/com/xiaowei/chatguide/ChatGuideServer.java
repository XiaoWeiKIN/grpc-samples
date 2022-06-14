package com.xiaowei.chatguide;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ChatGuideServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatGuideServer.class.getName());
    private final int port;
    private final Server server;

    public ChatGuideServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new ChatGuideService())
                .build();
    }
    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ChatGuideServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception{
        ChatGuideServer server = new ChatGuideServer(50051);
        server.start();
        server.blockUntilShutdown();


    }

    private static class ChatGuideService extends ChatGuideGrpc.ChatGuideImplBase{

        @Override
        public StreamObserver<ChatRequest> chat(final StreamObserver<ChatResponse> responseObserver) {

            return new StreamObserver<ChatRequest>() {
                @Override
                public void onNext(ChatRequest chatRequest) {
                    int userId = chatRequest.getUserId();
                    String msg = chatRequest.getMsg();

                    logger.info("客户端说: [" + msg + "]");

                    responseObserver.onNext(ChatResponse.newBuilder()
                            .setUserId(chatRequest.getUserId())
                            .setMsg("这是一条来自[服务端]的消息: 你好，收到了-" + userId + " 的消息. " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS").format(new Date()) + "\n")
                            .build());
                }

                @Override
                public void onError(Throwable t) {
                    logger.warn("[ChatGuideService] gRPC dealing error");

                }

                @Override
                public void onCompleted() {
                    logger.info("服务端处理完成");
                    // send message
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
