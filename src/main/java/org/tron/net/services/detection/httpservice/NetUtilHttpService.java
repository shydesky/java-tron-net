package org.tron.net.services.detection.httpservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.stereotype.Component;

/**
 * @program: java-tron-net
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2018-07-20
 **/


@Component
@Slf4j
public class NetUtilHttpService implements Service {

    private int port = 9999;

    private Server server;

    @Override
    public void init() {

    }

    @Override
    public void start() {
        try {
            server = new Server(port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            context.addServlet(new ServletHolder(new GetAllNodeServlet()), "/getallnode");
            server.start();
            logger.info("Http Server start successfully!");
        } catch (Exception e) {
            logger.error("IOException: {}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            logger.debug("IOException: {}", e.getMessage());
        }
    }
}
