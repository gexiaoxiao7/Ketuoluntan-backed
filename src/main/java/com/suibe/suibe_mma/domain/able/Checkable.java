package com.suibe.suibe_mma.domain.able;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 检查表主键接口
 * @param <T> 表类
 * @param <R> 主键类型
 */
public interface Checkable<T extends Checkable<T, R>, R> {

    /**
     * 检查主键
     * @param id 主键
     * @param service 相关服务类
     * @return 表类信息
     * @throws RuntimeException id为空或无效
     */
    T checkPrimaryKey(
            R id,
            IService<T> service
    ) throws RuntimeException;

    /**
     * 检查主键
     * @param ids 主键列表
     * @param service 相关服务类
     * @return 表类信息列表
     * @throws RuntimeException ids为空或无效
     */
    List<T> checkPrimaryKey(
            List<R> ids,
            IService<T> service
    ) throws RuntimeException;
}
