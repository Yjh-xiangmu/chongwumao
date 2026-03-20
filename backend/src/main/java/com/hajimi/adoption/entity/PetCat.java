package com.hajimi.adoption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pet_cat")
public class PetCat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String earTag;
    private String nickname;
    private String breed;
    private String age;
    private Integer gender; // 0-未知, 1-公, 2-母
    private String healthStatus;
    private Integer status; // 0-待领养, 1-已被申请, 2-已领养
    private String description;
    private String coverImage;
    private Long creatorId; // 建档人(救助员)ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String photoUrls;
}