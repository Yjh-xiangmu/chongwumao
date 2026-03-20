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
    private Long userId; // 领养人ID
    private Long catId;  // 猫咪ID
    private String reason; // 领养理由
    private String experience; // 养宠经验
    private Integer status; // 0-待审核, 1-审核通过, 2-已驳回, 3-已完成
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
}