package com.example.skillexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.skillexchange.entity.Message;

/**
 * 私信记录表 Mapper接口
 * 对应数据库表：message
 * 
 * 继承MyBatis-Plus的BaseMapper，自带增删改查方法
 * 私信必须内置敏感词检测，含敏感词的消息仍然保存留证
 */
public interface MessageMapper extends BaseMapper<Message> {

}
