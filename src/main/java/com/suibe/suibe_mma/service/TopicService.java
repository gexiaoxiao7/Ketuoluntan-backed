package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.exception.TopicException;

public interface TopicService extends IService<Topic> {

    /**
     * 上传题目
     * @param topicUploadRequest 上传的题目信息
     * @return 用户更新后信息
     * @throws TopicException 未上传上传者id、题目标题为空
     */
    User upload(TopicUploadRequest topicUploadRequest) throws TopicException;
}
