package com.finruntech.frt.fits.fitsbpmservice.activiti.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * stop service fitsbpm
 * Created by yinan.zhang on 2017/10/23.
 */
@RestController
public class StopMainController {

    @Autowired
    private ApplicationContext ctx;

    @GetMapping("stopbpm")
    public void main() {
        if ((ctx instanceof ConfigurableWebApplicationContext)) {
            ((ConfigurableWebApplicationContext)ctx).close();
        }
    }


}
