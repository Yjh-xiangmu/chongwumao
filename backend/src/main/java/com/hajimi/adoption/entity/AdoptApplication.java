package com.hajimi.adoption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("adopt_application")
public class AdoptApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long catId;
    private String reason;
    private String experience;
    private String housingCondition;
    private String feedingPlan;
    private String proofImages;
    private String signature;
    // 状态: 0-待审核, 1-审核通过(待签约), 2-已驳回, 3-已完成领养, 4-已取消
    private Integer status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
    private LocalDateTime signTime;
}