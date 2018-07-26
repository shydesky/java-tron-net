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
        int number = 0;
        if(detect.beforeDetect()){
            number = detect.doDetect();
        }else{
           logger.error("detect boot failure!");
        }
        logger.info("one round ends and all node detected number is: " + number);
    }

    public static void main(String args[]){
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.setAllowCircularReferences(false);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(beanFactory);
        context.register(DefaultConfig.class);
        context.refresh();

        NetUtilHttpService httpApiService = context.getBean(NetUtilHttpService.class);
        httpApiService.start();

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

        boolean[] flag = {false};
        NetAssitUtil util = new NetAssitUtil();
        util.detectExecutor.scheduleWithFixedDelay(() -> {
            logger.info("当前时间：" + String.valueOf(System.currentTimeMillis() / 1000));
            try {
                util.detectAllNode(detect[0]);
                flag[0] = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 300, TimeUnit.SECONDS);


/*        while(true){
            try {
                Thread.sleep(10000);
                logger.info("getAllNode:" + String.valueOf(detect[0].getAllNode().size()));
            }catch(InterruptedException e){

            }
        }*/
    }
}