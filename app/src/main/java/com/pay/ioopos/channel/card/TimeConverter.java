package com.pay.ioopos.channel.card;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeConverter extends StdConverter<Long, String> {
    @Override
    public String convert(Long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time * 1000));
    }
}
