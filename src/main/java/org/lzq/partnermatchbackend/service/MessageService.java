package org.lzq.partnermatchbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.lzq.partnermatchbackend.model.domain.Message;
import org.lzq.partnermatchbackend.model.vo.MessageVO;

import java.util.List;

/**
* @author liangzhiquan
* @description 针对表【message(消息表)】的数据库操作Service
* @createDate 2024-08-13 17:36:19
*/
public interface MessageService extends IService<Message> {
    /**
     * 获取当前用户的消息列表
     * @param request 当前会话
     * @return List<MessageVO>
     */
    List<MessageVO> listMessages(HttpServletRequest request);

    /**
     * 根据 发送方id 和 接收方id 获取历史记录
     * @param fromUserId 发送方id
     * @param toUserId 接收方id
     * @return List<MessageVO>
     */
    List<MessageVO> getUserHistoryMessage(Long fromUserId, Long toUserId);

    /**
     * 根据 发送方id 和 房间id 获取历史记录
     * @param fromUserId 发送方id
     * @param toRoomId 房间id
     * @return List<MessageVO>
     */
    List<MessageVO> getRoomHistoryMessage(Long fromUserId, Long toRoomId);
}
