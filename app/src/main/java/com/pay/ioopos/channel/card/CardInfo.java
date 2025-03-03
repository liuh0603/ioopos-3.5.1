package com.pay.ioopos.channel.card;

/**
 * 卡信息
 * @author    Moyq5
 * @since  2020/11/4 16:47
 */
public class CardInfo extends CardBase {
    /**
     * 旧卡序列号
     */
    private String prevUid;

    private CardUser user;

    private CardStat stat;

    public CardStat getStat() {
        return stat;
    }

    public void setStat(CardStat stat) {
        this.stat = stat;
    }

    public String getPrevUid() {
        return prevUid;
    }

    public void setPrevUid(String prevUid) {
        this.prevUid = prevUid;
    }

    public CardUser getUser() {
        return user;
    }

    public void setUser(CardUser user) {
        this.user = user;
    }
}
