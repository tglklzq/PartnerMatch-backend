package org.lzq.partnermatchbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.lzq.partnermatchbackend.mapper.MessageMapper;
import org.lzq.partnermatchbackend.model.domain.Message;
import org.lzq.partnermatchbackend.model.domain.Team;
import org.lzq.partnermatchbackend.model.domain.User;
import org.lzq.partnermatchbackend.model.domain.UserTeam;
import org.lzq.partnermatchbackend.model.vo.MessageVO;
import org.lzq.partnermatchbackend.service.MessageService;
import org.lzq.partnermatchbackend.service.TeamService;
import org.lzq.partnermatchbackend.service.UserService;
import org.lzq.partnermatchbackend.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author liangzhiquan
* @description 针对表【message(消息表)】的数据库操作Service实现
* @createDate 2024-08-13 17:36:19
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{
    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;

    @Override
    public List<MessageVO> listMessages(HttpServletRequest request) {
        List<MessageVO> result = new ArrayList<>();
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getUserId();

        // 存储发送者ID或群聊ID对应的最新消息
        Map<Long, Message> latestMessagesMap = new HashMap<>();

        // 查询接收的私聊消息
        List<Message> messageList = this.query()
                .select("receive_user_id, message_id, send_user_id, team_id, content, send_time, receive_type, send_type, read_time, type, cancel_time, is_cancel, is_read")
                .eq("receive_user_id", userId)
                .orderByAsc("send_time")
                .list();

        // 查询用户参与的群聊
        List<UserTeam> userTeamList = userTeamService.query()
                .eq("user_id", userId)
                .list();

        // 处理群聊消息
        userTeamList.forEach(userTeam -> {
            Long teamId = userTeam.getTeamId();
            List<Message> roomMessageList = this.query()
                    .eq("receive_user_id", teamId)
                    .orderByAsc("send_time")
                    .list();

            // 确保将最新的群聊消息更新到latestMessagesMap
            roomMessageList.forEach(message -> {
                if (!latestMessagesMap.containsKey(teamId) ||
                        message.getSendTime().after(latestMessagesMap.get(teamId).getSendTime())) {
                    latestMessagesMap.put(teamId, message);
                }
            });
        });

        // 处理私聊消息
        messageList.forEach(message -> {
            Long sendUserId = message.getSendUserId();
            if (!latestMessagesMap.containsKey(sendUserId) ||
                    message.getSendTime().after(latestMessagesMap.get(sendUserId).getSendTime())) {
                latestMessagesMap.put(sendUserId, message);
            }
        });

        // 构建返回的MessageVO列表
        latestMessagesMap.values().forEach(message -> {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(message, messageVO);

            // 判断接收方类型是私聊还是群聊
            Integer receiveType = message.getReceiveType();
            if (receiveType == null) {
                receiveType = 0;
            }
            if (receiveType == 1) {
                // 处理群聊消息
                Team team = teamService.getById(message.getReceiveUserId());
                if (team != null) {
                    messageVO.setAvatarUrl(team.getTeamImage());
                    messageVO.setUserName(team.getName());
                } else {
                    messageVO.setAvatarUrl("defaultAvatarUrl");
                    messageVO.setUserName("Unknown Team");
                }
            } else {
                // 处理私聊消息
                User user = userService.getById(message.getSendUserId());
                if (user != null) {
                    messageVO.setAvatarUrl(user.getAvatarUrl());
                    messageVO.setUserName(user.getUsername());
                } else {
                    messageVO.setAvatarUrl("defaultAvatarUrl");
                    messageVO.setUserName("Unknown User");
                }
            }
            result.add(messageVO);
        });

        return result;
    }



    private void processMessages(List<Message> messages, Map<Long, MessageVO> messageMap) {
        messages.forEach(message -> {
            Long receiveUserId = message.getReceiveUserId();
            Integer receiveType = message.getReceiveType();
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(message, messageVO);

            if (receiveType != null && receiveType == 1) {
                // 处理群聊消息
                Team team = teamService.getById(receiveUserId);
                if (team != null) {
                    messageVO.setAvatarUrl(team.getTeamImage());
                    messageVO.setUserName(team.getName());
                } else {
                    messageVO.setAvatarUrl("defaultAvatarUrl");
                    messageVO.setUserName("Unknown Team");
                }
            } else {
                // 处理私聊消息
                User user = userService.getById(message.getSendUserId());
                if (user != null) {
                    messageVO.setAvatarUrl(user.getAvatarUrl());
                    messageVO.setUserName(user.getUsername());
                } else {
                    messageVO.setAvatarUrl("defaultAvatarUrl");
                    messageVO.setUserName("Unknown User");
                }
            }

            // 将最新的消息放入map
            messageMap.put(message.getSendUserId(), messageVO);
        });
    }



    @Override
    public List<MessageVO> getUserHistoryMessage(Long fromUserId, Long toUserId) {
        List<MessageVO> result = new ArrayList<>();
        List<Message> messageList = this.query()
                .and(i -> i.eq("receive_user_id", toUserId).eq("send_user_id", fromUserId))
                .or(i -> i.and(j -> j.eq("receive_user_id", fromUserId).eq("send_user_id", toUserId)))
                .orderByAsc("send_time")
                .list();

        messageList.forEach(message -> {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(message, messageVO);
            // 通过发送方id查询头像和姓名
            User sendUserInfo = userService.getById(message.getSendUserId());
            messageVO.setUserName(sendUserInfo.getUsername());
            messageVO.setAvatarUrl(sendUserInfo.getAvatarUrl());
            result.add(messageVO);
        });
        return result;
    }

    @Override
    public List<MessageVO> getRoomHistoryMessage(Long fromUserId, Long toRoomId) {
        List<MessageVO> result = new ArrayList<>();
        // 查询房间号下的所有消息
        List<Message> messageList = this.query()
                .eq("receive_user_id", toRoomId)
                .orderByAsc("send_time")
                .list();

        messageList.forEach(message -> {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(message, messageVO);
            // 通过发送方id查询头像和姓名
            User sendUserInfo = userService.getById(message.getSendUserId());
            messageVO.setUserName(sendUserInfo.getUsername());
            messageVO.setAvatarUrl(sendUserInfo.getAvatarUrl());
            result.add(messageVO);
        });
        return result;
    }
}




