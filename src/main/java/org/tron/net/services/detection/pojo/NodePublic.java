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
        this.ipList.add("47.254.144.25:18888");
        this.ipList.add("47.254.146.147:18888");
        this.ipList.add("47.254.16.55:18888");
        this.ipList.add("52.14.86.232:18888");
    }

}
