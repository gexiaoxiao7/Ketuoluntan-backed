package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.exception.TopicException;

import java.util.List;

/**
 * 题目服务类接口
 */
public interface TopicService extends IService<Topic> {

    /**
     * 上传题目
     * @param topicUploadRequest 上传的题目信息
     * @return 用户更新后信息
     * @throws TopicException id为空或无效、题目标题为空
     */
    User upload(TopicUploadRequest topicUploadRequest) throws TopicException;

    /**
     * 取消点赞或添加点赞
     * @param topicId 题目唯一标识
     * @param id 用户唯一标识
     * @return 题目信息
     * @throws TopicException id为空或无效、点赞更新失败、topicId为空或无效
     */
    Topic like(Long topicId, Integer id) throws TopicException;

    /**
     * 根据用户唯一标识获取所有题目
     * @param userId 用户唯一标识
     * @return 题目列表
     * @throws TopicException 用户id无效或为空
     */
    List<Topic> getAllTopicByUserId(Integer userId) throws TopicException;
}
