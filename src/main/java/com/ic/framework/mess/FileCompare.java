package com.ic.framework.mess;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class FileCompare {

    public static void main(String[] args) {
        new FileCompare().compare(new File("d:", "testsrc"), new File("d:", "testdes"));
    }

    private void compare(JSONArray src, JSONArray des) {
        JSONArray ja1 = (JSONArray) src.clone();
        JSONArray ja2 = (JSONArray) des.clone();
        Iterator<Object> iterator = ja1.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (ja2.contains(o)) {
                iterator.remove();
                ja2.remove(o);
            }
        }
        System.out.println("src: " + ja1.toJSONString());
        System.out.println("des: " + ja2.toJSONString());
    }

    private void compare(File src, File des) {
        Arrays.stream(ArrayUtils.addAll(src.listFiles(File::isFile), des.listFiles(File::isFile))).map(file -> file.getName().substring(0, file.getName().lastIndexOf("_"))).distinct().parallel().forEach(s -> {
            JSONArray jaSrc = read(src.listFiles(file -> file.getName().startsWith(s)));
            JSONArray jaDes = read(des.listFiles(file -> file.getName().startsWith(s)));
            compare(jaSrc, jaDes);
        });
    }

    private JSONArray read(File[] files) {
        JSONArray ja = new JSONArray();
        if (files != null) {
            for (File file : files) {
                String content = null;
                try {
                    content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ja.addAll(JSONArray.parseArray(content));
            }
        }
        return ja;
    }
}
