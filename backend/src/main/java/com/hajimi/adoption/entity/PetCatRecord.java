package com.hajimi.adoption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pet_cat_record")
public class PetCatRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long catId;
    private String recordType; // 驱虫、疫苗、就医、日常
    private String content;
    private String mediaUrls; // 记录附带的照片
    private Long creatorId;
    private LocalDateTime createTime;
}