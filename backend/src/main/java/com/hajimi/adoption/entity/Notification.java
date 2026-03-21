package com.hajimi.adoption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 接收通知的用户ID
    private Long userId;

    // 通知大类: 0-系统通知 1-社区通知
    private Integer type;

    // 通知子类型
    // 系统类: APPLY_SUBMIT / APPLY_APPROVED / APPLY_REJECTED / SIGN_SUCCESS
    // 社区类: COMMENT_LIKE / COMMENT_REPLY / POST_COMMENT
    private String subType;

    private String title;
    private String content;

    // 0-未读 1-已读
    private Integer isRead;

    // 关联业务ID（申请ID / 帖子ID / 评论ID）
    private Long relatedId;

    private LocalDateTime createTime;
}