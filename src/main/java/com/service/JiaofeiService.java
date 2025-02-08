package com.service;

import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.JiaofeiEntity;
import java.util.Map;

/**
 * 缴费表 服务类
 * @author 
 * @since 2021-04-05
 */
public interface JiaofeiService extends IService<JiaofeiEntity> {

    /**
    * @param params 查询参数
    * @return 带分页的查询出来的数据
    */
     PageUtils queryPage(Map<String, Object> params);
}