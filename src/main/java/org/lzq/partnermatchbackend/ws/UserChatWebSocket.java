package org.lzq.partnermatchbackend.ws;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.lzq.partnermatchbackend.model.domain.Message;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.service.MessageService;
import org.lzq.partnermatchbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/user_chat/{fromUserId}/{toUserId}")
@Component
public class UserChatWebSocket {


    private static ApplicationContext applicationContext;
    @Autowired
    public  void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 记录当前在线连接数
     */
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private MessageService messageService;
    private UserService userService;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("fromUserId") String fromUserId,
                       @PathParam("toUserId") String toUserId) {
        // 该userId必须是自己的userId，建立一个连接
        sessionMap.put(fromUserId, session);
        log.info("有新用户加入，userId={}, 当前在线人数为：{}", fromUserId, sessionMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * 后台收到客户端发送过来的消息
     * onMessage 是一个消息的中转站
     * 接受 浏览器端 socket.send 发送过来的 json数据
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session,
                          @PathParam("fromUserId") String fromUserId,
                          @PathParam("toUserId") String toUserId) {
        log.info("服务端收到用户userId={}的消息:{}", fromUserId, message);
        JSONObject obj = JSON.parseObject(message);
        String content = obj.getString("content");
        Integer receiveType = obj.getInteger("receiveType");
        Integer sendType = obj.getInteger("sendType");

        Session toSession = sessionMap.get(String.valueOf(toUserId));
        if (toSession != null) {
            // 确保 applicationContext 不为空
            if (applicationContext == null) {
                log.error("ApplicationContext is not set.");
                return;
            }

            // 获取 UserService 和 User 实例
            UserService userService = applicationContext.getBean(UserService.class);
            User user = userService.getUserById(Integer.valueOf(fromUserId));

            // 组装消息
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sendUserId", fromUserId);
            jsonObject.put("receiveUserId", toUserId);
            jsonObject.put("avatarUrl", user.getAvatarUrl());
            jsonObject.put("userName", user.getUsername());
            jsonObject.put("content", content);
            jsonObject.put("position", "left");

            this.sendMessage(jsonObject.toString(), toSession);
            log.info("发送给用户userId={}，消息：{}", toUserId, jsonObject);
        } else {
            log.info("发送失败，未找到用户userId={}的session", toUserId);
        }

        // 消息持久化
        Message messageEntity = new Message();
        messageEntity.setSendUserId(Long.valueOf(fromUserId));
        messageEntity.setReceiveUserId(Long.valueOf(toUserId));
        messageEntity.setContent(content);
        messageEntity.setReceiveType(receiveType);
        messageEntity.setSendType(sendType);
        MessageService messageService = applicationContext.getBean(MessageService.class);
        messageService.save(messageEntity);
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("fromUserId") String fromUserId) {
        sessionMap.remove(fromUserId);
        log.info("有一连接关闭，移除userId={}的用户session, 当前在线人数为：{}", fromUserId, sessionMap.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

    /**
     * 服务端发送消息给所有客户端
     */
    private void sendAllMessage(String message) {
        try {
            for (Session session : sessionMap.values()) {
                log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

}
