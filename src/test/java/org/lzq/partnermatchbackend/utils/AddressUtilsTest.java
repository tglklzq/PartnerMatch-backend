package org.lzq.partnermatchbackend.utils;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class AddressUtilsTest {

    @Test
    void getIp() {
        try {
            URL realUrl = new URL("http://whois.pconline.com.cn/ipJson.jsp");
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setReadTimeout(6000);
            conn.setConnectTimeout(6000);
            conn.setInstanceFollowRedirects(false);
            int code = conn.getResponseCode();
            StringBuilder sb = new StringBuilder();
            List<String> list = new ArrayList<>();
            if (code == 200) {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                list.add(sb.substring(sb.indexOf("ip") + 5, sb.indexOf("pro") - 3));
                String pro = sb.substring(sb.indexOf("pro") + 6, sb.indexOf("proCode") - 3);
                String city = sb.substring(sb.indexOf("city") + 7, sb.indexOf("cityCode") - 3);
                list.add(pro+city);
            }
            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getIpAddr() {
    }

    @Test
    void testGetIp() {
    }

    @Test
    void getAddress() {
    }
}