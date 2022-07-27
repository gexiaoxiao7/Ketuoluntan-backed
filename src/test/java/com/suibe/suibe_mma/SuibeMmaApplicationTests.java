package com.suibe.suibe_mma;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SuibeMmaApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.setSql("updateTime = now()").eq("id", 1);
        userMapper.update(null, wrapper);
    }


    @Test
    void test() {
        System.out.println("?".matches("\\pP"));
    }

}
