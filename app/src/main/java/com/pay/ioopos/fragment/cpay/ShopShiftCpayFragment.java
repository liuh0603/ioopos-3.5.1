package com.pay.ioopos.fragment.cpay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.channel.cpay.QueryShopInfoRequest;
import com.pay.ioopos.channel.cpay.QueryShopInfoResponse;
import com.pay.ioopos.channel.cpay.ShopInfo;
import com.pay.ioopos.channel.cpay.StaffInfo;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.sqlite.StoreFactory;

import java.util.List;

/**
 * 腾讯云支付切换店员
 * @author    Moyq5
 * @since  2020/7/29 19:11
 */
public class ShopShiftCpayFragment extends TipVerticalFragment implements BindState {
    private final String staffId;

    public ShopShiftCpayFragment(String staffId) {
        super(WAIT, "正在切换店员");
        this.staffId = staffId;
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        StaffInfo staff;
        try {
            staff = queryStaff();
        } catch (Exception e) {
            speak("店员切换失败");
            dispatch(FAIL, e.getMessage());
            return;
        }
        StoreFactory.cpayStore().setStaffId(staffId);
        StoreFactory.cpayStore().setStaffName(staff.getStaff_name());
        onSuccess(staff.getStaff_name());
    }

    private void onSuccess(String staffName) {
        speak("店员切换成功");
        dispatch(SUCCESS, "店员：" + staffName);
    }

    private StaffInfo queryStaff() throws Exception {
        String outShopId = StoreFactory.cpayStore().getOutShopId();
        if (null == outShopId) {
            throw new Exception("门店信息不存在");
        }
        QueryShopInfoRequest req = new QueryShopInfoRequest();
        req.setPage_num(1);
        req.setPage_size(100);
        QueryShopInfoResponse res = MyCloudPay.getInstance().queryShopInfo(req);
        if (res.getTotal_count() == 0) {
            throw new Exception("门店信息不存在");
        }
        List<ShopInfo> shops = res.getShop_infos();
        ShopInfo shop = null;
        for (ShopInfo item: shops) {
            if (item.getOut_shop_id().equals(outShopId)) {
                shop = item;
                break;
            }
        }
        if (null == shop) {
            throw new Exception("门店信息不存在");
        }
        List<StaffInfo> staffs = shop.getStaff_infos();
        StaffInfo staff = null;
        for (StaffInfo item: staffs) {
            if (item.getStaff_id().equals(staffId)) {
                staff = item;
                break;
            }
        }
        if (null == staff) {
            throw new Exception("店员不存在");
        }
        return staff;
    }

}
