package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.exception.ReplyException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回复相关操作类
 */
@RestController
@RequestMapping("/reply")
public class ReplyController {
    /**
     * 写回复
     * @param replyWriteRequest 回复请求对象
     * @return 提示信息
     */
    @PostMapping("/writeReply")
    public String writeReply(@RequestBody ReplyWriteRequest replyWriteRequest) {
        if (replyWriteRequest == null) {
            return "请求失败";
        }
        try {

            return "回复成功";
        } catch (ReplyException e) {
            return e.getMessage();
        }
    }
}
