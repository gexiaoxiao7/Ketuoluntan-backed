package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Message;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.MessageIdRequest;
import com.suibe.suibe_mma.domain.request.MessageReportRequest;
import com.suibe.suibe_mma.domain.request.MessageWriteRequest;
import com.suibe.suibe_mma.enumeration.MessageEE;
import com.suibe.suibe_mma.exception.MessageException;
import com.suibe.suibe_mma.mapper.MessageMapper;
import com.suibe.suibe_mma.service.MessageService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 信息服务实现类
 */
@Service
@Transactional
public class MessageServiceImpl
        extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    /**
     * 注入userService
     */
    @Resource
    private UserService userService;

    @Override
    public List<Message> getReceiveMessages(@NotNull User current) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper
                .eq("receiveId", current.getId())
                .eq("receiveDelete", false);
        return list(wrapper);
    }

    @Override
    public List<Message> getSendMessages(@NotNull User current) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper
                .eq("sendId", current.getId())
                .eq("sendDelete", false);
        return list(wrapper);
    }

    @Override
    public Message read(
            @NotNull MessageIdRequest messageIdRequest,
            User current) throws MessageException {
        try {
            Integer messageId = messageIdRequest.getMessageId();
            Message message = checkId(Message.class, messageId, this);
            Integer id = current.getId();
            boolean sendFlag = id.equals(message.getSendId());
            boolean receiveFlag = id.equals(message.getReceiveId());

            if (sendFlag && message.getSendDelete()) {
                MessageEE.MESSAGE_DELETE.throwE();
            }
            if (receiveFlag && !message.getReceiveDelete()) {
                if (!message.getIsRead()) {
                    message.setIsRead(true);
                    notSetNull(message, "isRead");
                    if (!updateById(message)) {
                        MessageEE.MESSAGE_READ_UPDATE_FAILED.throwE();
                    }
                    message = getById(messageId);
                }
            } else if (receiveFlag) {
                MessageEE.MESSAGE_DELETE.throwE();
            }
            if (!sendFlag && !receiveFlag) {
                MessageEE.MESSAGE_UNABLE_READ.throwE();
            }
            return message;
        } catch (RuntimeException | IllegalAccessException e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    @Override
    public void report(
            @NotNull MessageReportRequest messageReportRequest,
            @NotNull User current) throws MessageException {
        try {
            Integer sendId = messageReportRequest.getSendId();
            String messageContent = messageReportRequest.getMessageContent();
            userHelp(sendId, userService);
            if (!sendId.equals(current.getId())) {
                MessageEE.MESSAGE_NOT_CURRENT.throwE();
            }
            if (messageContent == null || "".equals(messageContent)) {
                MessageEE.MESSAGE_CONTENT_EMPTY.throwE();
            }
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userRole", 2);
            List<User> managers = userService.list(queryWrapper);

            managers.stream().filter(user -> !user.getId().equals(sendId)).forEach(user -> {
                Message message = new Message();
                message.setSendId(sendId);
                message.setMessageContent(messageContent);
                message.setReceiveId(user.getId());
                if (!save(message)) {
                    MessageEE.MESSAGE_SAVE_FAILED.throwE();
                }
            });
        } catch (RuntimeException e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    @Override
    public List<Message> getReadOrUnRead(
            @NotNull User current,
            boolean isRead) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper
                .eq("receiveId", current.getId())
                .eq("isRead", isRead)
                .eq("receiveDelete", false);
        return list(wrapper);
    }

    @Override
    public void delete(
            User current,
            @NotNull Message message) throws MessageException {
        try {
            Integer id = current.getId();
            Message getMessage = checkId(Message.class, message.getMessageId(), this);
            boolean sendFlag = getMessage.getSendId().equals(id);
            boolean receiveFlag = getMessage.getReceiveId().equals(id);
            boolean sendDelete = getMessage.getSendDelete();
            boolean receiveDelete = getMessage.getReceiveDelete();
            messageDelete(getMessage, sendFlag, sendDelete, receiveDelete, "sendDelete", this);
            messageDelete(getMessage, receiveFlag, receiveDelete, sendDelete, "receiveDelete", this);
        } catch (RuntimeException e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    @Override
    public void write(
            @NotNull MessageWriteRequest writeRequest,
            User current) throws MessageException {
        try {
            Integer sendId = writeRequest.getSendId();
            if (sendId == null || !sendId.equals(current.getId())) {
                MessageEE.MESSAGE_SEND_ID_NOT_EQUAL.throwE();
            }
            Integer receiveId = writeRequest.getReceiveId();
            checkId(User.class, receiveId, userService);
            if (sendId.equals(receiveId)) {
                MessageEE.MESSAGE_CAN_NOT_SAME.throwE();
            }
            String messageContent = writeRequest.getMessageContent();
            Message message = new Message();
            message.setSendId(sendId);
            message.setReceiveId(receiveId);
            message.setMessageContent(messageContent);
            if (!save(message)) {
                MessageEE.MESSAGE_SAVE_FAILED.throwE();
            }
        } catch (RuntimeException e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    @Override
    public List<Message> getMessages(User current) {
        List<Message> receiveMessages = getReceiveMessages(current);
        List<Message> sendMessages = getSendMessages(current);
        List<Message> messages = new ArrayList<>(receiveMessages.size() + sendMessages.size());
        messages.addAll(receiveMessages);
        messages.addAll(sendMessages);
        return messages;
    }
}
