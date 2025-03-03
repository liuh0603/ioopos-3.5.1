package com.pay.ioopos.sqlite;

import static com.pay.ioopos.sqlite.ApayHelper.TB_SETTING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝云支付相关配置数据实现
 * @author    Moyq5
 * @since  2020/12/11 15:35
 */
public class ApayStoreImpl implements ApayStore {
    private Map<String, String> map = new HashMap<>();
    private ApayHelper helper;
    public ApayStoreImpl(Context context) {
        helper = new ApayHelper(context, "apay.db", null, 3);
    }
    private void set(String name, String value) {
        SQLiteDatabase wdb = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        int count = wdb.update(TB_SETTING, cv, "name=?", new String[]{name});
        if (count == 0) {
            cv.put("name", name);
            wdb.insert(TB_SETTING, null, cv);
        }
        map.put(name, value);
    }
    private String get(String name) {
        String value = map.get(name);
        if (null != value) {
            return value;
        }
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("select value from " + TB_SETTING + " where name=?", new String[]{name});
        if (c.moveToNext()) {
            value = c.getString(c.getColumnIndex("value"));
            c.close();
        }
        map.put(name, value);
        return value;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        set("serverUrl", serverUrl);
    }

    @Override
    public String getServerUrl() {
        return "https://ecogateway.alipay-eco.com/gateway.do";
        //return "http://ecogatewaysit.alipay-eco.com/gateway.do";
        //return get("serverUrl");
    }

    @Override
    public void setAppId(String appId) {
        set("appId", appId);
    }

    @Override
    public String getAppId() {
        return "B43578771883";
        //return "B08430531194";
        //return get("appId");
    }

    @Override
    public void setMid(String mid) {
        set("mid", mid);
    }

    @Override
    public String getMid() {
        //return "20191129183400001459645700000072";
        return get("mid");
    }

    @Override
    public void setStoreId(String storeId) {
        set("storeId", storeId);
    }

    @Override
    public String getStoreId() {
        //return "20200901145300003099286200000650";
        return get("storeId");
    }

    @Override
    public void setPriKey(String priKey) {
        set("priKey", priKey);
    }

    @Override
    public String getPriKey() {
        return "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC5/frvsU49QAy1dA9NXJfbT8Le+FBNK9h69wzJ1nWAQ2y16MWdsibOlNoIfUDMcyUn0+2qPF1Ycq1g/Rp9mx7UnCmZcagMvbbNYIFvo46CnM1hYMrgJmQz+y3YzeE8FbJx3gNYd/c0FtIavrE9ixfy7BFiz9aOWgF3pYTDXI7DzDpqOWxmjGmiiJcyu+Kn/6XNsYTvfIAHZo9XnX9A885rXHOLpF2+oiAMs/F06OjQngZEMy3v0uaH4I56UiCpcpxXhCH98cKw6p38/E3JuktVD21GBbcYDI9who5zO5+FuueMMPknVB1wzQz/mQ+tsQD+QbY7cAR89+SEzsBPZXSjAgMBAAECggEBAIkKOfqiCaPXrYWFHkFCGCHfgxYGoCQ8SiVQInwVKD55b4AZIoIu4akUxBG9OCFsNdRCsrrb/2tME7OCyiICSZUd+wi0Vb4NisOId9ZqDHbwUeOODe1aXDpwuDcmeu5qjjCYBJ8KxXn71pMeLjnKYrr6dVT5Dn2Uk8A0lL3dVNOuRR+OEm2b/DBkdo695gPW/sZ9fGFZaiDwb0TWXbTfjvqlEIo6C3Yj0gtfquNFqgoX6TRKotE4FRzBA8wOtikLboJGTXXuUxe9JPyOvwes9g5RefU9rL2iv31f7cMJW9Cem4/4ARfSs4GJY+YScbdrFYJQqcfLq8U1yBrlPADgOQECgYEA4aeVvfNDuyUn9tX0xf9sxl08bgTl+bBBacIhUfrA1aeU/sXVJr8av88VqCIARsLuN+74cgMDgujdDqVzJnt3cpE+cMhQV8UUYCy7C5cUo7rfHV1Sth7oeWXV0Ne0hcn5EpgSkuy2X08mVZE/1SBFeWIk08VlawMb3ZQAFE1elI0CgYEA0wD35svvDhsluLClPeaC6XY+yWSem2gjiLc3HcbOsFr/48/DOXUDr2LY3eusrSqvtCRvX1GbyEg0sc8BPAwK/DxoN5de3hSi2vEP6ggAbcbvodBQbWdUnvKadSbbQJ1raoGr9zHb8ZjuR4zvwC7xUGcmUiFF33OTAe6zaB2zGe8CgYAa5B/CLTVLHhhvH/w1hETwNrg23DqT0N9avRm/bUSBsaDd47GNFoUXipQoHzUtOZmt7LqKy7Ulap69WhYo5dnMO2bXheedLg1JkMsRjIWXJXPlSfPLZE+L+AjNBIes0e7uulSsUa1CGmqVE+Qpf9kHqBx/aCy33fz6IOMJiFbXGQKBgCo8IWLjIyCRsoU7ZnfzcfTFJGAvccftml89PR0KtEh8jofNCGix0WEDyiDxkYi/6JeIS3qq3SHDwjhtFPQt/OzsMngeJnERdF+arPLNUzil/0VGazLa4gdGzD/y3SQb3mpdNkyugDXlxJwxy7OOMeNEIlmCK3hErAJRAzwt5bGlAoGBALsJiN72MzcavXHvPzegM1bo2jPvOvPUr9i6rWFCfqqb72Xs5M19lWtJDPFzNlDoiAXzstsGybP/svZUIMcR+uwj191T1nAR/zW/2pXBMKE929QdGdWgNrcnGNyVSsp91eLDAsgPWlPjp/t39ZnGk6a+dRJ9bF9ZX/m9kqBKRf0k";
        //return "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCCToY6TukF4ouWU8VEL+gI+vJBl0+tAWdMbyO93QDsnHzxtfKiHnbKCXV16lO6Za/WerujOOE+hr+XSUu9Lt4thTVXrUb+4aSwsp0rL/6B6Tf2MHCTicyMPtY9HAQgOEeUKuyHbM2HAFyeH+dtICgeziuQGRaH7NLE8FGEQIKDcu8T4M3b5docSNZRb1h8zH7g8u9qlhpJ7yHG6hFdCrqjEYv6zWkXjlteLGpVnglV8x3JGn2IcyTzeE9xAuDeMHDQPVpU72nW+g49pWOOxI/S6xKnxgs14A2H5W9ZoBKe2F8CxczfGVMGB16X04dsti20rxKmNtRATs2G6/P8qC9NAgMBAAECggEAdpqIfBwE5xFbohlfbP/5v+rBg8f4gmzLm/tw2ciFpdeNtee5D6yQzLYtToVZbkhN8vdFQHxuMB4v1sClCm3VVjNv6PzTqPyyjQ4WFhAaJB6ljBRs8y0ym9g54edVLgprxEYJgf4bWCyRIG/DkkT5n4hiiEb9hfydnxlp6Olaoc81NUkm4Ufq55tL6DvI/xYU4TTxgKe7wXbTqA55Yh5War3inCotNwQVIktsvLMm7Cb6gaCH4BskUHAE1xRMTAYk0GJB6QHIASRNBk69VS3zquqFqyYxIt13f0+nq6FZZd1XWeWgpwbYXGOT3AvUAkLdxNj5QNsBKomK5j5RX8SSFQKBgQDOKqjGjfYwUQTIRkhHbCFlkvo7AbaE0plT2zDMJpc8dA2f7nKPfJbuMM6j06RM2n78IMR2AsnbyIQV3QSaR0wwtAHcFGIoQHXHYszDya4KDoMKQkWke7O9TC/S+29Rf98NJyPzBcM96Tb213rtXRdQakmj7thRNQiySOy2TgluowKBgQChzb8g2cRVjAydBogmoqkiKC84+KM95QSlFQezyc0YdXTy/wanu0J+uVx1L2IzT9aWU8PZm4bs20zIEGcY4L11hqOFYSGAfZ6hhptXVyO2G9N4owtMdCGQzCby02zAUUpDZOVTbqyb0BwsRV7sUpVZiNYUKMqjsCnvLThZ7vx5TwKBgG169dlKtbt+qp13xRY4c5uu6za+eCAcfdOsCEPBEnrF3h5Zz3gm3zdpr7ILx6oQNXLKK8nHPU57Mrkxfyo2Rl1umbY3FNDvOhxBeR9XUBaDEk82Vik8j3wsoxDU+I4860PezxZUrxOHbuqyDtNRpfnMF4L4aOLm2NFkLF+7HQMlAoGBAIOTGOIw05wxN6yVPDAWw+y3urbcUXqqel13vXyxFGvYT9KuGY5aE5eTSiEs9/D78mbqFPAmrdB8AHMMC5pKXyZr5xs2QhUHkfCN0lJy1OJovE10YGK6aPUjXmTGEsBNGlO1f1qaPBi0YcSKYMdR3IsjX9qi1S3IukD5h8JyObK3AoGARy4yBHuSCsA/Ras3GwULMz86rGcTkqw2xaB8AiLNF7PwJmvDN2fPiHUQq4WT2lRcMPbQ+pfwYsw1ZS6kWmue8qdhTfEcmAHCV55FIBBjJ9zV40hsQXnFNYzth3EO23ZZuiXiijm6VvR6S/cFvd/wTto2BSy3wUzbiYBVlPUf8No=";
        //return get("priKey");
    }

    @Override
    public void setPubKey(String pubKey) {
        set("pubKey", pubKey);
    }

    @Override
    public String getPubKey() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqbaNm26MXYROduQOdEBB5y1ApNc8iEGg6U/CHYgpW5zzjCZ17z5dgk6SHx5ar/eVcEM4vHMKIf15nLeCj1TNwTnxDqCpUNbyZJ90Kzkv+b5JoCEjIRlUtuantHUJfIkO/vAZ2npQBgx2mktwvv6EHvzf+dgYFeUA/h0/AwhltJ2Rf2G6t1HfKZmaEuqVFPCrNyLb1v2FeQrrDKpAk5eBlRmIb95c7oWEguiwPF9O8vbIldL8//YGSR+OfOCunr10ANvRTdjXx35Sv+sAWP/7uwODMcnuioOqH1zghE/hskp3iPRYVY3PT++5pb1a7YXCdLli6VGQ1LHzdwCTUTE2pwIDAQAB";
        //return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgk6GOk7pBeKLllPFRC/oCPryQZdPrQFnTG8jvd0A7Jx88bXyoh52ygl1depTumWv1nq7ozjhPoa/l0lLvS7eLYU1V61G/uGksLKdKy/+gek39jBwk4nMjD7WPRwEIDhHlCrsh2zNhwBcnh/nbSAoHs4rkBkWh+zSxPBRhECCg3LvE+DN2+XaHEjWUW9YfMx+4PLvapYaSe8hxuoRXQq6oxGL+s1pF45bXixqVZ4JVfMdyRp9iHMk83hPcQLg3jBw0D1aVO9p1voOPaVjjsSP0usSp8YLNeANh+VvWaASnthfAsXM3xlTBgdel9OHbLYttK8SpjbUQE7Nhuvz/KgvTQIDAQAB";
        //return get("pubKey");
    }

    @Override
    public void setOrderNoPrefix(String orderNoPrefix) {
        set("orderNoPrefix", orderNoPrefix);
    }

    @Override
    public String getOrderNoPrefix() {
        //return "TS";
        return get("orderNoPrefix");
    }

}
