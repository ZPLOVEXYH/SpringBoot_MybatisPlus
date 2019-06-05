package com.warrior.mapper;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.warrior.entity.Student;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
  *  Mapper 接口
 * </p>
 *
 * @author lqh
 * @since 2018-05-05
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    @Select("selectStudentByStuName")
    List<Student> selectStudentByStuName(String stuName);

    @Override
    Integer updateById(Student student);

    @Override
    Integer update(@Param("et") Student student, @Param("ew") Wrapper<Student> wrapper);

    @Override
    Integer deleteBatchIds(List<? extends Serializable> list);
}