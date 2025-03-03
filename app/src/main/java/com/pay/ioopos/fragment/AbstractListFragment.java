package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_0;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_1;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_3;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_4;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_5;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_6;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_7;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_8;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_9;
import static com.pay.ioopos.widget.Tip.TipType.WARN;

import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractListFragment extends AbstractNetworkFragment implements BindState, KeyInfoListener {
    private static final List<KeyInfo> numKeys = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    static {
        numKeys.add(KEY_NUM_0);
        numKeys.add(KEY_NUM_1);
        numKeys.add(KEY_NUM_2);
        numKeys.add(KEY_NUM_3);
        numKeys.add(KEY_NUM_4);
        numKeys.add(KEY_NUM_5);
        numKeys.add(KEY_NUM_6);
        numKeys.add(KEY_NUM_7);
        numKeys.add(KEY_NUM_8);
        numKeys.add(KEY_NUM_9);
    }

    private int prePage = 1;
    private int nowPage = 1;
    private boolean isCached = false;

    @Override
    public void onDetach() {
        super.onDetach();
        isCached = true;
    }

    @Override
    protected void execute() throws Exception {
        if (!lock.tryLock()) {
            return;
        }
        try {
            if (isCached) {
                isCached = false;
                return;
            }

            if (getListView().getAdapter().getCount() >= 300) {
                nowPage = prePage;
                toast("只能加载最近300条记录");
                return;
            }

            showLoading();
            List<Object> list;
            try {
                list = loadListData(nowPage);
            } catch (NoRecordException e) {
                noRecord();
                return;
            } catch (LoadFailException e) {
                onError(e.getMessage());
                return;
            } catch (InterruptedException e) {
                return;
            }
            uiExecute(() -> {
                ((ArrayAdapter)getListView().getAdapter()).addAll(list);
                getListView().setAdapter(getListView().getAdapter());
                getListView().setSelection(getListView().getAdapter().getCount() - list.size() - 1);
                hideLoading();
            });
        } finally {
            lock.unlock();
        }

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        ListView listView = getListView();
        if (numKeys.contains(keyInfo)) {
            int position = listView.getSelectedItemPosition();
            int num = Integer.parseInt(keyInfo.getValue());
            listView.setSelection(numPosition(position, num));
            return true;
        } else if (keyInfo == KeyInfo.KEY_UP) {
            listView.onKeyDown(KeyEvent.KEYCODE_DPAD_UP, new KeyEvent(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.ACTION_DOWN));
            return true;
        } else if (keyInfo == KeyInfo.KEY_DOWN) {
            loadNextPage();
            listView.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, new KeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.ACTION_DOWN));
            return true;
        } else if (keyInfo == KeyInfo.KEY_DOT) {
            loadFirstPage();
            listView.setSelection(0);
            return true;
        } else if (keyInfo == KeyInfo.KEY_ADD) {
            loadNextPage();
            listView.setSelection(listView.getAdapter().getCount() - 1);
            return true;
        } else if (keyInfo == KeyInfo.KEY_ENTER) {
            showDetail(listView.getSelectedItem());
            return true;
        } else if (keyInfo == KeyInfo.KEY_CANCEL) {
            back();
            return true;
        }

        return false;
    }

    private void loadNextPage() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            ListView listView = getListView();
            if (listView.getSelectedItemPosition() < listView.getAdapter().getCount() -1) {
                return;
            }
            if (nowPage == getTotalPage()) {
                toast("没有更多了");
                return;
            }
            prePage = nowPage;
            nowPage++;
        } finally {
            lock.unlock();
        }
        run();
    }

    private void loadFirstPage() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            ListView listView = getListView();
            if (listView.getSelectedItemPosition() != 0) {
                return;
            }
            ((ArrayAdapter<?>)listView.getAdapter()).clear();
            listView.setAdapter(listView.getAdapter());
            prePage = nowPage;
            nowPage = 1;
        } finally {
            lock.unlock();
        }
        run();
    }

    private int numPosition(int position, int num) {
        return numPosition(position, num, false);
    }

    private int numPosition(int position, int num, boolean recycle) {
        ListView listView = getListView();
        int newPosition = ((position + 1)/10 < 0 ? 0: ((position + 1)/10 * 10)) + ((num == 0 || ((position + 1)%10 > 0 && ((position + 1)%10%num == 0) || (position + 1)%10 > num)) ? 10 : 0) + num - 1;
        if (newPosition > listView.getAdapter().getCount() - 1) {
            if (!recycle) {
                return numPosition(0, num, true);
            }
            return position;
        }
        return newPosition;
    }

    protected abstract List<Object> loadListData(int nowPage) throws Exception;

    protected abstract ListView getListView();

    protected abstract int getTotalPage();

    protected abstract void back();

    protected abstract void showDetail(Object data);

    protected void noRecord() {
        setMainFragment(new TipVerticalFragment(WARN, "没有记录"));
    }

    public static class NoRecordException extends Exception {

    }

    public static class LoadFailException extends Exception {
        public LoadFailException(String message) {
            super(message);
        }
    }
}
