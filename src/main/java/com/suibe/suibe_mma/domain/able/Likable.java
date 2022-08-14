package com.suibe.suibe_mma.domain.able;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 能够进行点赞类继承的接口
 */
public interface Likable {
    /**
     * 点赞接口
     * @param userId 点赞用户唯一标识
     * @param flag 点赞与取消点赞标志
     * @param template redis模板类
     * @param key 键
     * @param service 相关服务类
     * @param userService 用户服务类
     * @return 点赞类信息
     * @throws RuntimeException 运行时异常类
     */
    Likable like(
            Integer userId,
            Integer flag,
            RedisTemplate<String, Object> template,
            String key,
            IService<? extends Likable> service,
            UserService userService
    ) throws RuntimeException;
}
