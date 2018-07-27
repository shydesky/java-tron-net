package org.tron.net.services.detection.httpservice;

/**
 * @program: java-tron-net
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2018-07-20
 **/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
            HashMap<String, ArrayList<String>> outcome = new HashMap<>();
            ArrayList<String> ipList = new ArrayList<>();
            for (Map.Entry<String, NodeHandler> entry : currentNetNode.entrySet())
            {
                ipList.add(entry.getKey().split(":")[0]);
            }
            JSONArray res = new JSONArray();
            res.add(ipList);
            outcome.put("ip", ipList);
            JSONObject json = new JSONObject();
            json.putAll(outcome);
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().println(json.toJSONString());
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
        }
    }
}