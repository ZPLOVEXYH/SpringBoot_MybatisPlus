package com.warrior.controler;

import com.warrior.entity.Student;
import com.warrior.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zp
 * @since 2018-05-05
 */
@Controller
@RequestMapping("/warrior/student")
public class StudentController {
    @Autowired
    IStudentService iStudentService;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        //insert
        Student student = new Student()
                .setStuName("zhangsan")
                .setStuNumber("54")
                .setAge(23);
        boolean res = iStudentService.insert(student);

        return res ? "success" : "fail";
    }

    @RequestMapping("/hello2")
    @ResponseBody
    public List<Student> hello2() {
        return iStudentService.selectStudentByStuName("linqihong");
    }

    @RequestMapping(value = "/hello3", method = RequestMethod.POST)
    @ResponseBody
    public Integer hello3(@RequestBody Student student) {
        return iStudentService.updateStudentById(student);
    }

    @RequestMapping(value = "/hello4")
    @ResponseBody
    public Integer hello4() {
        return iStudentService.updateStudent();
    }

    @RequestMapping(value = "/hello5")
    @ResponseBody
    public Integer hello5(Long... ids) {
        return iStudentService.deleteAllByIds(ids);
    }

    @RequestMapping(value = "/hello6", method = RequestMethod.POST)
    @ResponseBody
    public boolean hello6(@RequestBody List<Student> stus) {
        return iStudentService.insertBatch(stus);
    }
}
