package com.suibe.suibe_mma;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SuibeMmaApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private TopicService topicService;

    @Test
    void contextLoads() {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.setSql("updateTime = now()").eq("id", 1);
        userMapper.update(null, wrapper);
    }


    @Test
    void test() {
        User userInfo = userService.getById(2);
        userInfo.setUsername("test23");
        userInfo.setUpdateTime(null);
        userService.updateById(userInfo);
    }

    @Test
    void test2() {
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserAccount("12345679");
        userLoginRequest.setUserPassword("CC123456");
        User login = userService.login(userLoginRequest);
        System.out.println(login);
    }

    @Test
    void test3() {
        TopicUploadRequest topicUploadRequest = new TopicUploadRequest();
        topicUploadRequest.setUserId(1);
        topicUploadRequest.setTopicTitle("test2");
        topicService.upload(topicUploadRequest);
    }

    @Test
    void test4() {

    }

}
