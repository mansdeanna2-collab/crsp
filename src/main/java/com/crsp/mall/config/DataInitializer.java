package com.crsp.mall.config;

import com.crsp.mall.service.AdminService;
import com.crsp.mall.service.OrderService;
import com.crsp.mall.service.ProductDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 应用启动时初始化默认数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ProductDbService productDbService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminService adminService;

    @Override
    public void run(String... args) {
        // 初始化默认管理员
        adminService.initDefaultAdmin();
        
        // 初始化默认商品
        productDbService.initDefaultProducts();
        
        // 初始化示例订单
        orderService.initSampleOrders();
        
        log.info("数据初始化完成！后台管理地址: http://localhost:8080/admin");
        log.info("默认管理员账号: admin  密码: admin123");
    }
}
