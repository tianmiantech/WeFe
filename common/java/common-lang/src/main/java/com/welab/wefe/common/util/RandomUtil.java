package com.welab.wefe.common.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

/**
 * @author yuxin.zhang
 */
public class RandomUtil extends RandomStringUtils {

    public static List<char[]> randomCharList = new ArrayList<>();
    static {
        randomCharList.add("MNBVCXZLKJHGFDSAPOIUYTREWQ".toCharArray());
        randomCharList.add("qwertyuiopasdfghjklzxcvbnm".toCharArray());
        randomCharList.add("0123456789".toCharArray());
        randomCharList.add("~!@#$%^&*".toCharArray());

    }

    public static String generateRandomStrongPwd(int len){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int x = 0; x < len; ++x) {
            int i = x;
            if(x > 3) {
                i = random.nextInt(randomCharList.size());
            }

            sb.append(randomCharList.get(i)[random.nextInt(randomCharList.get(i).length)]);
        }

        return sb.toString();
    }

    public static String generateRandomPwd(int len){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int x = 0; x < len; ++x) {
            int i;
            if(x < 3) {
                i = 0;
            } else if(x >=3 && x < 6) {
                i = 1;
            } else {
                i = 2;
            }

            sb.append(randomCharList.get(i)[random.nextInt(randomCharList.get(i).length)]);
        }

        return sb.toString();
    }

}
