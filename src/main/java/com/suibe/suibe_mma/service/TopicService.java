package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.SearchTitleRequest;
import com.suibe.suibe_mma.domain.request.TopicIdRequest;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.exception.TopicException;

import java.util.List;

/**
 * 题目服务类接口
 */
public interface TopicService
        extends IService<Topic> {

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
    Topic like(
            Long topicId,
            Integer id) throws TopicException;

    /**
     * 根据用户唯一标识获取所有题目
     * @param userId 用户唯一标识
     * @return 题目列表
     * @throws TopicException 用户id无效或为空
     */
    List<Topic> getAllTopicByUserId(Integer userId) throws TopicException;

    /**
     * 根据题目信息获取其作者信息
     * @param topic 题目信息
     * @return 作者信息
     * @throws TopicException 题目id无效或为空
     */
    User getAuthor(Topic topic) throws TopicException;

    /**
     * 确定点赞信息
     * @param topic 题目信息
     * @param id 用户唯一标识
     * @return 是否点赞
     * @throws TopicException 用户id无效或为空
     */
    Integer topicLikeHelp(
            Topic topic,
            Integer id) throws TopicException;

    /**
     * 作者或管理员删除题目
     * @param topicIdRequest 题目id类
     * @param user 用户信息
     * @param isAuthor 是否是作者
     * @return 题目信息
     * @throws TopicException 作者id无效或为空，删除题目失败，id不匹配
     */
    Topic deleteByAuthorOrNot(
            TopicIdRequest topicIdRequest,
            User user,
            boolean isAuthor) throws TopicException;

    /**
     * 作者或管理员批量删除题目
     * @param ids 题目id列表
     * @param user 用户唯一标识
     * @param isAuthor 是否是作者
     * @return 题目信息列表
     * @throws TopicException 作者id无效或为空，删除题目失败，id不匹配
     */
    List<Topic> deleteBatchByAuthorOrNot(
            List<Long> ids,
            User user,
            boolean isAuthor) throws TopicException;

    /**
     * 根据搜索题目信息查询题目
     * @param searchTitleRequest 查询题目相关请求类
     * @return 题目列表
     * @throws TopicException 无搜索结果
     */
    List<Topic> searchTitle(SearchTitleRequest searchTitleRequest) throws TopicException;

    /**
     * 更新题目信息
     * @param topic 题目信息
     * @param current 当前用户信息
     * @return 更新后题目信息
     * @throws TopicException 更新失败、更新后题目title为空
     */
    Topic updateTopicInfo(Topic topic, User current) throws TopicException;

    /**
     * 管理员取消或设为精选
     * @param topic 题目信息
     * @param current 当前用户
     * @return 题目信息
     * @throws TopicException 更新失败，不为管理员
     */
    Topic star(Topic topic, User current) throws TopicException;
}
