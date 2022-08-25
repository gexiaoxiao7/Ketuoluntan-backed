package com.suibe.suibe_mma;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.request.*;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import com.suibe.suibe_mma.util.ControllerUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 项目测试类
 */
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
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper
                .orderByDesc("score")
                .orderByAsc("createTime")
                .ne("userRole", 2);
        userService.list(wrapper).forEach(System.out::println);
    }

    @Test
    void test5() {
        List<Topic> list = topicService.list();
        list.forEach(topic -> System.out.println(topicService.topicLikeHelp(topic, 1)));
    }

    @Test
    void test6() {
        String[] split = ("topicId:" + 1).split(":");
        System.out.println(Arrays.toString(split));
    }

    @Test
    void test7() {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(4);
        integers.add(5);
        List<User> users = userService.listByIds(integers);
        users.forEach(System.out::println);
    }

}
