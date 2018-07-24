package org.tron.net.services.detection.httpservice;

/**
 * @program: java-tron-net
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2018-07-20
 **/

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.net.services.detection.services.NodeDetection;
import org.tron.net.services.detection.services.NodeHandler;


@Slf4j
@Component
public class GetAllNodeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            HashMap<String, NodeHandler> currentNetNode = NodeDetection.currentNetNode;
            String str = "";
            for (Map.Entry<String, NodeHandler> entry : currentNetNode.entrySet())
            {
                str += "<h1>" + entry.getKey() + "</h1>";
            }
            response.setContentType("text/html");
            response.getWriter().println(str);
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
        }
    }
}