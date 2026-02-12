package com.crsp.mall.config;

import com.crsp.mall.service.AdminService;
import com.crsp.mall.service.ProductDbService;
import com.crsp.mall.service.PromotionService;
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
    private AdminService adminService;

    @Autowired
    private PromotionService promotionService;

    @Override
    public void run(String... args) {
        try {
            // 初始化默认管理员
            adminService.initDefaultAdmin();
        } catch (Exception e) {
            log.error("初始化默认管理员失败: {}", e.getMessage(), e);
        }

        try {
            // 初始化默认商品
            productDbService.initDefaultProducts();
        } catch (Exception e) {
            log.error("初始化默认商品失败: {}", e.getMessage(), e);
        }

        try {
            // 初始化示例促销活动
            promotionService.initSamplePromotions();
        } catch (Exception e) {
            log.error("初始化示例促销活动失败: {}", e.getMessage(), e);
        }

        log.info("数据初始化完成！后台管理地址: http://localhost:8080/admin");
    }
}
