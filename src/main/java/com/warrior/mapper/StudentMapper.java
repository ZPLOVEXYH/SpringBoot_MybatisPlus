package com.warrior.mapper;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.warrior.entity.Student;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    /**
     * <p>
     * 根据 whereEntity 条件，更新记录
     * </p>
     *
     * @param entity        实体对象
     * @param updateWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    @Override
    Integer update(Student student, Wrapper<Student> wrapper);

    Integer updateAgeById(@Param("age") int age, @Param("id") int id);

    Student getById(long id);


}