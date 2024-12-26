package com.mengpeng.wheelsurf;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mengpeng.mphelper.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main2Activity extends AppCompatActivity {

    private List<String> listUrl;
    private List<Prize> list;
    private List<Bitmap> listBitmap;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                lotteryView.setPrizes(list);
                lotteryView.surfaceCreated(lotteryView.getHolder());
            }
        }
    };
    private LotteryView lotteryView;
    private EditText lottery_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ToastUtils.getInstance().initToast(this);

        lotteryView = findViewById(R.id.lotteryView);
        lottery_et = findViewById(R.id.lottery_et);

        listUrl = new ArrayList<>();
        list = new ArrayList<>();
        listBitmap = new ArrayList<>();

        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_muwu@3x.png");
        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_thanks@3x.png");
        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_20nl@3x.png");

        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_shuwa@3x.png");
        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_chouyici@3x.png");
        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_xiyi@3x.png");

        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_10nl@3x.png");
        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_thanks@3x.png");
        listUrl.add("https://souget.oss-cn-hangzhou.aliyuncs.com/img/farm/lo_500nl@3x.png");


        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 9; i++) {
                    Prize lottery = new Prize();
                    lottery.setName("奖品下标：" + i);
                    lottery.setId(i + 1);
                    lottery.setBgColor(BitmapFactory.decodeResource(getResources(), R.mipmap.bg));
                    Bitmap bitmap = null;
                    try {
                        bitmap = Glide.with(Main2Activity.this).asBitmap().load(listUrl.get(i)).submit().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    lottery.setIcon(bitmap);
                    list.add(lottery);
                }
                handler.sendEmptyMessage(1);
            }
        }).start();

        lotteryView.setOnStartClickListener(new LotteryView.OnStartClickListener() {
            @Override
            public int onStartClick() {
                String number = lottery_et.getText().toString();
                if (TextUtils.isEmpty(number) || number.equals("4")) {
                    ToastUtils.onSuccessShowToast("中奖下标不能为空或4，默认设置为0");
                    return 0;
                } else {
                    return Integer.parseInt(number);
                }
            }
        });

        lotteryView.setOnTransferWinningListener(new LotteryView.OnTransferWinningListener() {
            @Override
            public void onWinning(int position) {
                ToastUtils.onSuccessShowToast(list.get(position).getName());
            }
        });

    }
}
