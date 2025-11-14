package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.angelbacked.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus的BaseMapper已提供CRUD方法，无需额外编写
}
