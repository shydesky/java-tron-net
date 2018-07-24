package org.tron.net.services.detection.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.net.common.config.DefaultConfig;
import org.tron.net.services.detection.httpservice.NetUtilHttpService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class NetAssitUtil {

    public ScheduledExecutorService detectExecutor = Executors.newSingleThreadScheduledExecutor();

    public void detectAllNode(NodeDetection detect){
        detect.beforeDetect();
        detect.doDetect();
    }

    public static void main(String args[]){
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.setAllowCircularReferences(false);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(beanFactory);
        context.register(DefaultConfig.class);
        context.refresh();

        NodeDetection detect[] = new NodeDetection[1];
        while(true){
            detect[0] = context.getBean(NodeDetection.class);
            if(detect[0].getMessageSender() != null){
                break;
            }else{
                try {
                    Thread.sleep(300);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        NetAssitUtil util = new NetAssitUtil();
        util.detectExecutor.scheduleWithFixedDelay(() -> {
            try {
                util.detectAllNode(detect[0]);
            } catch (Throwable t) {
                logger.error("Exception in log worker", t);
            }
        }, 10, 300, TimeUnit.SECONDS);


        NetUtilHttpService httpApiService = context.getBean(NetUtilHttpService.class);
        httpApiService.start();

    }
}