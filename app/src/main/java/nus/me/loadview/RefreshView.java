package nus.me.loadview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by nus on 16-10-8.
 */

public class RefreshView extends LoadView {


    private int mCurrentState;
    private ViewGroup mContent;
    private int mTopMargin = Dp2Px.Dp2Px(getContext(),15);

    private ImageView iv;
    private TextView tv;
    private ProgressBar pb;

    public RefreshView(Context context) {
        this(context,null);
    }

    public RefreshView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        measureChild(mContent,MeasureSpec.makeMeasureSpec(width,MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(height,MeasureSpec.AT_MOST));
        mContent.layout(0,height-mContent.getMeasuredHeight()-mTopMargin,width,height-mTopMargin);


    }

    private void init() {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        mContent = ll;
        addView(mContent);
        initContentView();

    }

    private void initContentView() {
        mContent.removeAllViews();
        ImageView iv_icon = new ImageView(getContext());
        LinearLayout.LayoutParams ivParmas = new LinearLayout.LayoutParams(Dp2Px.Dp2Px(getContext(), 20), Dp2Px.Dp2Px(getContext(), 20));
        ivParmas.setMargins(0, 0, Dp2Px.Dp2Px(getContext(), 10), 0);
        iv_icon.setLayoutParams(ivParmas);
        iv_icon.setImageResource(R.drawable.ic_pull);
        mContent.addView(iv_icon);


        ProgressBar progress = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressParmas =
                new LinearLayout.LayoutParams(Dp2Px.Dp2Px(getContext(), 20), Dp2Px.Dp2Px(getContext(), 20));
        progressParmas.setMargins(0, 0, Dp2Px.Dp2Px(getContext(), 10), 0);
        progress.setLayoutParams(progressParmas);
        progress.setVisibility(View.GONE);
        mContent.addView(progress);

        TextView tv_tips = new TextView(getContext());
        tv_tips.setText("下拉刷新...");
        mContent.addView(tv_tips);

        iv = iv_icon;
        tv = tv_tips;
        pb = progress;

        mCurrentState = STATE_NORMAL;


    }


    @Override
    public void setRefreshing() {
        if (mCurrentState != STATE_REFRESH){
            iv.setVisibility(GONE);
            pb.setVisibility(VISIBLE);
            tv.setText("内容加载中...");
        }

    }

    @Override
    public void setNormal() {
        if (mCurrentState != STATE_NORMAL){
            iv.setVisibility(VISIBLE);
            pb.setVisibility(GONE);

            if (mCurrentState == STATE_PULLING){
                ObjectAnimator.ofFloat(iv,"rotation",180,0).start();
            }

            tv.setText("下拉刷新...");
            mCurrentState = STATE_NORMAL;

        }


    }

    @Override
    public void setPulling() {
        if (mCurrentState != STATE_PULLING){
            iv.setVisibility(VISIBLE);
            pb.setVisibility(GONE);
            if (mCurrentState == STATE_NORMAL){
                ObjectAnimator.ofFloat(iv,"rotation",0,180).start();
            }
            tv.setText("松开刷新...");
        }


    }



    @Override
    public void setComplete() {
        if (mCurrentState != STATE_COMPLETE) {
            iv.setVisibility(View.GONE);
            pb.setVisibility(View.GONE);
            tv.setText("加载完成");
            mCurrentState = STATE_COMPLETE;
        }
    }


}
