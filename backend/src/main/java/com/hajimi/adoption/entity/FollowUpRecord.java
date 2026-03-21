package com.hajimi.adoption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("follow_up_record")
public class FollowUpRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 关联的领养申请ID
    private Long applicationId;
    // 猫咪ID
    private Long catId;
    // 领养人ID
    private Long userId;

    // 健康状况描述
    private String healthStatus;
    // 生活照片（逗号分隔URL）
    private String photoUrls;
    // 备注
    private String remark;

    // 提交类型: 0-领养人主动提交, 1-救助员代录（电话回访）
    private Integer submitType;
    // 如果是救助员代录，记录救助员ID
    private Long volunteerId;

    private LocalDateTime createTime;
}