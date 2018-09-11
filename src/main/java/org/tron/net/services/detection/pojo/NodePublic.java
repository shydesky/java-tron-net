package org.tron.net.services.detection.pojo;

import org.springframework.stereotype.Component;
import java.util.ArrayList;

/**
 * @program: java-tron-net
 * @description: public node we can detect
 * @author: shydesky@gmail.com
 * @create: 2018-07-18
 **/

@Component
public class NodePublic {

    private ArrayList<String> ipList = new ArrayList<>();

    public NodePublic(){
        if(getIpList().size()==0){
            setIpList();
        }
    }

    public ArrayList<String> getIpList(){
        return ipList;
    }
    public void setIpList(){
        this.ipList.add("47.90.240.201:18888");
        this.ipList.add("47.89.188.246:18888");
        this.ipList.add("47.90.208.195:18888");
        this.ipList.add("47.89.188.162:18888");
        this.ipList.add("47.89.185.110:18888");
        this.ipList.add("47.89.183.137:18888");
    }

}
