package com.crsp.mall.config;

import com.crsp.mall.service.AdminService;
import com.crsp.mall.service.OrderService;
import com.crsp.mall.service.ProductDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 应用启动时初始化默认数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

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
        
        System.out.println("============================================");
        System.out.println("数据初始化完成!");
        System.out.println("默认管理员: admin / admin123");
        System.out.println("后台管理地址: http://localhost:8080/admin");
        System.out.println("============================================");
    }
}
