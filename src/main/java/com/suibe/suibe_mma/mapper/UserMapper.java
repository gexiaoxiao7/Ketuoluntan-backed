package com.suibe.suibe_mma.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suibe.suibe_mma.domain.User;
import org.apache.ibatis.annotations.Select;

/**
 * 用户mapper类
 */
public interface UserMapper
        extends BaseMapper<User> {
    /**
     * 根据userAccount查询用户是否存在
     * @param userAccount 账户名
     * @return 数据条数
     */
    @Select("select count(*) from mma_user where userAccount = #{userAccount} and isDelete = 0")
    int selectByUserAccount(String userAccount);
}
