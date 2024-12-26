package com.mengpeng.wheelsurf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LotteryView extends SurfaceView implements Callback {

    /**
     * holder
     */
    private SurfaceHolder mHolder;
    public static final String TAG = "LotteryView";


    private List<Prize> prizes;
    private boolean flags;

    private int lottery; //设置中奖号码

    private int current = 0; //抽奖开始的位置

    private int count = 0; //旋转次数累计

    private int countDown; //倒计次数，快速旋转完成后，需要倒计多少次循环才停止


    private int MAX = 60; //最大旋转次数

    private OnTransferWinningListener listener;

    public void setOnTransferWinningListener(OnTransferWinningListener listener) {
        this.listener = listener;
    }

    public interface OnStartClickListener {
        int onStartClick();
    }

    private OnStartClickListener startClickListener;

    public void setOnStartClickListener(OnStartClickListener onStartClickListener) {
        this.startClickListener = onStartClickListener;
    }

    public interface OnTransferWinningListener {
        /**
         * 中奖回调
         *
         * @param position
         */
        void onWinning(int position);
    }


    /**
     * 设置中奖号码
     *
     * @param lottery
     */
    public void setLottery(int lottery) {
        if (prizes != null && Math.round(prizes.size() / 2) == 0) {
            throw new RuntimeException("开始抽奖按钮不能设置为中奖位置！");
        }
        this.lottery = lottery;
    }

    /**
     * 设置奖品集合
     *
     * @param prizes
     */
    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouch(event);
        return super.onTouchEvent(event);
    }

    /**
     * 触摸
     */
    public void handleTouch(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX() - getLeft(), (int) event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Prize prize = prizes.get(Math.round(prizes.size()) / 2);
                if (prize.isClick(touchPoint, getMeasuredWidth())) {
                    if (!flags) {
                        setStartFlags(true);
                        if (prize.listener != null) {
                            prize.click();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private class SurfaceRunnable implements Runnable {
        @Override
        public void run() {
            while (flags) {
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas();
                    drawBg(canvas);
                    drawTransfer(canvas);
                    drawPrize(canvas);
                    controllerTransfer();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null)
                        mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }


    /**
     * 1.绘制所有奖品背景
     * 2.绘制奖品
     */
    private void drawBg(Canvas canvas) {
        //任意两个宫格之间的距离
        int spaceBetween = dp2px(8);
        //边长
        int side = (getMeasuredWidth() - spaceBetween * 4) / 3;

        int x1, y1, x2, y2;
        //开平方,每行就有N个宫格
        int len = (int) Math.sqrt(prizes.size());
        //绘制底盘
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FCEDC6"));
        canvas.drawRect(new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight()), paint);

        paint.setStyle(Paint.Style.FILL);                   //空心效果

        RectF r2 = new RectF();                           //RectF对象
        r2.left = 0;                                 //左边
        r2.top = 0;                                 //上边
        r2.right = getWidth();                                   //右边

        r2.bottom = getWidth();                              //下边
        paint.setColor(Color.parseColor("#320406"));
        paint.setAntiAlias(true);//抗锯齿
        canvas.drawRoundRect(r2, spaceBetween, spaceBetween, paint);        //绘制圆角矩形

        for (int index = 0; index < len * len; index++) {
            Prize prize = prizes.get(index);
            //宫格的左上X坐标
            x1 = (side + spaceBetween) * (index % len) + spaceBetween;
            //宫格的左上Y坐标
            y1 = (side + spaceBetween) * (index / len) + spaceBetween;
            x2 = x1 + side;
            y2 = y1 + side;
            //绘制每一个宫格的背景
            if (index != 4) {
                Rect rect = new Rect(x1, y1, x2, y2);
                prize.setRect(rect);
                canvas.drawBitmap(prize.getBgColor(), null, rect, null);
            }
        }

    }

    //绘制旋转的奖品背景
    private void drawTransfer(Canvas canvas) {
        //任意两个宫格之间的距离的一半
        int spaceBetween = dp2px(8);
        //边长
        int side = (getMeasuredWidth() - spaceBetween * 4) / 3;

        int x1, y1, x2, y2;
        //开平方,每行就有N个宫格
        int len = (int) Math.sqrt(prizes.size());
        //旋转顺序0 > 1 > 2 > 5 > 8 > 7 > 6 > 3 > 0
        current = next(current, len);

        //宫格的左上X坐标
        x1 = (side + spaceBetween) * (current % len) + spaceBetween;
        //宫格的左上Y坐标
        y1 = (side + spaceBetween) * (current / len) + spaceBetween;
        x2 = x1 + side;
        y2 = y1 + side;

        Rect rect = new Rect(x1, y1, x2, y2);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.bg_white),
                null, rect, null);
    }

    public int dp2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

    //控制旋转的速度
    private void controllerTransfer() {
        if (count > MAX) {
            countDown++;
            SystemClock.sleep(count * 5);
        } else {
            SystemClock.sleep(count * 2);
        }

        count++;
        if (countDown > 2) {
            if (lottery == current) {
                countDown = 0;
                count = 0;
                setStartFlags(false);
                if (listener != null) {
                    //切换到主线程中运行
                    post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWinning(current);
                        }
                    });
                }
            }
        }
    }

    public void setStartFlags(boolean flags) {
        this.flags = flags;
    }

    //绘制奖品背景
    private void drawPrize(Canvas canvas) {
        //任意两个宫格之间的距离
        int spaceBetween = dp2px(8);
        //边长
        int side = (getMeasuredWidth() - spaceBetween * 4) / 3;

        int x1, y1, x2, y2;
        //开平方,每行就有N个宫格
        int len = (int) Math.sqrt(prizes.size());

        for (int index = 0; index < len * len; index++) {
            Prize prize = prizes.get(index);
            //宫格的左上X坐标
            x1 = (side + spaceBetween) * (index % len) + spaceBetween;
            //宫格的左上Y坐标
            y1 = (side + spaceBetween) * (index / len) + spaceBetween;
            x2 = x1 + side;
            y2 = y1 + side;
            //绘制每一个宫格的奖品
            Rect rect = new Rect(x1, y1, x2, y2);
            prize.setRect(rect);
            Bitmap icon = prize.getIcon();

//            BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(R.mipmap.lottery1);
//            Bitmap bitmap = drawable.getBitmap();
            canvas.drawBitmap(icon, null, rect, null);


        }

    }

    public void start() {
        setLottery(lottery);
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new SurfaceRunnable());
    }

    //下一步
    public int next(int current, int len) {//8 , 3

        if (current + 1 < len) {
            return ++current;
        }
        if ((current + 1) % len == 0 & current < len * len - 1) {
            return current += len;
        }
        if (current % len == 0) {
            return current -= len;
        }
        if (current < len * len) {
            return --current;
        }
        return current;
    }


    public LotteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setZOrderOnTop(true);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
    }

    public LotteryView(Context context) {
        this(context, null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = mHolder.lockCanvas();
            drawBg(canvas);
            drawPrize(canvas);

            Prize prize = prizes.get(Math.round(prizes.size() / 2));
            prize.setListener(new Prize.OnClickListener() {
                @Override
                public void click() {
                    if (startClickListener != null) {
                        lottery = startClickListener.onStartClick();
                        start();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                mHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setStartFlags(false);
    }

    /**
     * 重新测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(width, width);
    }
}