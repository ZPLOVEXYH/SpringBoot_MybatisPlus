package com.warrior.serviceImpl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.warrior.entity.Student;
import com.warrior.mapper.StudentMapper;
import com.warrior.service.IStudentService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lqh
 * @since 2018-05-05
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {


    @Override
    public List<Student> selectStudentByStuName(String Student) {
        return this.baseMapper.selectStudentByStuName(Student);
    }

    @Override
    public Integer updateStudentById(Student student) {
        return this.baseMapper.updateById(student);
    }

    @Override
    public Integer updateStudent() {
        EntityWrapper<Student> entityWrapper = new EntityWrapper<>();
        entityWrapper.between("age", 43, 45);

        Student student = new Student();
        student.setStuName("zhangsan");

        return this.baseMapper.update(student, entityWrapper);
    }

    @Override
    public Integer deleteAllByIds(Long... ids) {
        return this.baseMapper.deleteBatchIds(Arrays.asList(ids));
    }

}
