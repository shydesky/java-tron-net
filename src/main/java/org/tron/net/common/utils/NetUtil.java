package org.tron.net.common.utils;

/**
 * @program: java-tron-net
 * @description: tronprotocol net util
 * @author: shydesky@gmail.com
 * @create: 2018-07-18
 **/

public class NetUtil {
    
    /** 
    * @Description: calculate the distance between two the owner Node and the target Node. 
    * @Param: 
      * @param ownerId  the NodeId of owner
      * @param targetId the NodeId of target
    * @return: int 
    * @Author: shydesky@gmail.com
    * @Date: 2018/7/18 
    */ 
    public static int distance(byte[] ownerId, byte[] targetId) {
        byte[] h1 = targetId;
        byte[] h2 = ownerId;

        byte[] hash = new byte[Math.min(h1.length, h2.length)];

        for (int i = 0; i < hash.length; i++) {
            hash[i] = (byte) (((int) h1[i]) ^ ((int) h2[i]));
        }
        int d = 256;
        for (byte b : hash) {
            if (b == 0) {
                d -= 8;
            } else {
                int count = 0;
                for (int i = 7; i >= 0; i--) {
                    boolean a = (b & (1 << i)) == 0;
                    if (a) {
                        count++;
                    } else {
                        break;
                    }
                }
                d -= count;
                break;
            }
        }
        return d;
    }

    /** 
    * @Description: generate a nodeid with given nodeid
    * @Param:
     * @param hexString
     * @param distance
    * @return: java.lang.String 
    * @Author: shydesky@gmail.com
    * @Date: 2018/7/18 
    */ 
    public static String mockNodeIdWithDistance(String nodeIdString, int distance){
        int len = nodeIdString.length() / 2;
        int m = (distance-1) / 8;
        int n = (distance-1) % 8;
        if(distance == 0){
            return nodeIdString;
        }

        String oldHexString = nodeIdString.substring(len - 2 * m - 2, len - 2 * m);

        int oldHexInt = Integer.valueOf(oldHexString, 16);
        oldHexInt ^= (1<<(n));
        String newHexString;
        if(oldHexInt <= 0xf){
            newHexString = "0" + Integer.toHexString(oldHexInt);
        }else{
            newHexString = Integer.toHexString(oldHexInt);
        }
        return nodeIdString.substring(0,len - 2 * m - 2) + newHexString + nodeIdString.substring(len - 2 * m,nodeIdString.length());
    }
}