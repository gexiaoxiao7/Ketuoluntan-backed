create table mma_message
(
    messageId      int auto_increment
        primary key,
    messageContent text             null comment '消息内容',
    sendId         int              null comment '发送用户的ID',
    createTime     datetime         null comment '创建时间',
    updateTime     datetime         null comment '更新时间',
    receiveId      int              null comment '收到用户ID',
    isDelete       bit default b'0' null comment '逻辑删除',
    isRead         bit default b'0' null comment '0-未读1-已读',
    sendDelete     bit default b'0' null comment '发送者删除',
    receiveDelete  bit default b'0' null comment '收到者删除'
)
    comment '消息，发送者为普通用户或者管理员，收消息默认为全体管理员';