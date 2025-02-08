package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.JiaofeiEntity;

import com.service.JiaofeiService;
import com.entity.view.JiaofeiView;
import com.service.YonghuService;
import com.entity.YonghuEntity;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 缴费表
 * 后端接口
 * @author
 * @email
 * @date 2021-04-05
*/
@RestController
@Controller
@RequestMapping("/jiaofei")
public class JiaofeiController {
    private static final Logger logger = LoggerFactory.getLogger(JiaofeiController.class);

    @Autowired
    private JiaofeiService jiaofeiService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;


    //级联表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isNotEmpty(role) && "用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        params.put("orderBy","id");
        PageUtils page = jiaofeiService.queryPage(params);

        //字典表数据转换
        List<JiaofeiView> list =(List<JiaofeiView>)page.getList();
        for(JiaofeiView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiaofeiEntity jiaofei = jiaofeiService.selectById(id);
        if(jiaofei !=null){
            //entity转view
            JiaofeiView view = new JiaofeiView();
            BeanUtils.copyProperties( jiaofei , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(jiaofei.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JiaofeiEntity jiaofei, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,jiaofei:{}",this.getClass().getName(),jiaofei.toString());
        Wrapper<JiaofeiEntity> queryWrapper = new EntityWrapper<JiaofeiEntity>()
            .eq("yonghu_id", jiaofei.getYonghuId())
            .eq("jiaofei_types", jiaofei.getJiaofeiTypes())
            .eq("jiaofei_money", jiaofei.getJiaofeiMoney())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiaofeiEntity jiaofeiEntity = jiaofeiService.selectOne(queryWrapper);
        if(jiaofeiEntity==null){
            Date date = new Date();
            jiaofei.setInsertTime(date);
            jiaofei.setCreateTime(date);
            if(jiaofei.getShifouTypes() == 1){
                jiaofei.setUpdateTime(date);
            }
//            jiaofei.setShifouTypes(2);
//          String role = String.valueOf(request.getSession().getAttribute("role"));
//          if("".equals(role)){
//              jiaofei.set
//          }
            jiaofeiService.insert(jiaofei);
            return R.ok();
        }else {
            return R.error(511,"该学生的该缴费类型的缴费金额已经存在");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JiaofeiEntity jiaofei, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,jiaofei:{}",this.getClass().getName(),jiaofei.toString());
        if(jiaofei.getShifouTypes() == null || jiaofei.getShifouTypes() != 1){
            return R.error(511,"请您选择缴费");
        }else{
            //根据字段查询是否有相同数据
            Wrapper<JiaofeiEntity> queryWrapper = new EntityWrapper<JiaofeiEntity>()
                .notIn("id",jiaofei.getId())
                .andNew()
                .eq("yonghu_id", jiaofei.getYonghuId())
                .eq("jiaofei_types", jiaofei.getJiaofeiTypes())
                .eq("jiaofei_money", jiaofei.getJiaofeiMoney())
                .eq("shifou_types", jiaofei.getShifouTypes())
                ;
            logger.info("sql语句:"+queryWrapper.getSqlSegment());
            JiaofeiEntity jiaofeiEntity = jiaofeiService.selectOne(queryWrapper);
            jiaofei.setUpdateTime(new Date());
            if(jiaofeiEntity==null){
                jiaofeiService.updateById(jiaofei);//根据id更新
                return R.ok();
            }else {
                return R.error(511,"该学生的该缴费类型的缴费金额已经存在");
            }
        }
    }


    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        jiaofeiService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }



}

