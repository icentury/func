package com.ic.framework.mess;

public class IPHandler {

    public static void main(String[] args) {
        String ipRange = "24.0.255.";

        int subStart = 126;
        int subEnd = 254;

        for (int i = 0; i <= subEnd - subStart; i++) {
            System.out.println(ipRange + (subStart + i));
        }
    }
}
