package com.pay.ioopos.support.face;

import com.google.gson.Gson;

/**
 * @author: Administrator
 * @date: 2024/3/7
 */

public class BdFaceSdkTestData {
    public static String loadData = "{\n" +
            "        \"userId\": \"20240123\",\n" +
            "        \"userName\": \"liuh\",\n" +
            "        \"unit\": \"student\",\n" +
            "        \"num\": \"18688977441\",\n" +
            "        \"state\": 1,\n" +
            "        \"descr\": \"刘浩\",\n" +
            "        \"groupNo\": \"0\",\n" +
            "        \"setting\": \"0\",\n" +
            "        \"faceFeature\": \"5ubzEQpabRWftJCYSWw1nHyWs38Ompx75k9yisiVF4ztuo4wsy/vNN9+Ijk3+ge8BYap3xihAaSC0Xeqz1MjLDQI6C9U8uDUiCdi2Pnr4yP1RBLAQMZGRFzt0Unzv3POL+xAcHQtXnbMU8EHCAKLffLO7B8Lp9vkbuVk7Ip2gOwkt02RUbIuawvlbxgvO2FjRAKuALY4ZntOmesJYbO2DKOyUDBbACU1/U/UuHCICDxQDxdfQjPyJy0ZCdcmhoOtvib00AT16FXDjrVZCJV1IyXSj0AHvtZFOjbjyZSfd8zPEhtxcOJP9GW6QnjWvgr+Bl/S4P2bS+QhfXAXaqGn7PC1r+8LHX4VvmU+mEzknxy6zKUANZcchCfl2oiHWJAMwigeMbHVKzTYjIe4CQhAPBnnMCIiaBUlBbFSqhSZaS2PI/Ov9Jnaq0qBWdp1aahcTXbuv1GEUcQtRxfJ7C4/zGca3XEnQtJ2Sv2T+HIEAnz4n8GfzFlHZEg6O+kXtW1se3PYkCqEYZQw3agZgTErnOwS1AFSOvmESd0Q96ookg38x2cw8twitMZbhDifn1A8Ed/tIXgqBicHlFwo8ZSWLMNvSC/iv2ar10ksWkr7z9ywOI5Bv13XxFprzsjfDVZN6sbT8MGgUnWHdff4TQsw/Md5duBYCLHkjX1+aOvSp+w=\"\n" +
            "}";

    public static <T> T clz2Json(String string, Class<T> clz) {
        try {
            return new Gson().fromJson(string, clz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
