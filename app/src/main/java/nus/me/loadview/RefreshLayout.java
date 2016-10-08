package nus.me.loadview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.support.v7.widget.RecyclerView;

/**
 * Created by nus on 16-10-8.
 */

public class RefreshLayout extends FrameLayout{

    private RefreshAndLoadImp refreshAndLoadImp;
    private int mFinalHeight = Dp2Px.Dp2Px(getContext(),50);
    private int mRefreshHeight = (int) (mFinalHeight*1.3d);

    private RefreshView refreshView;
    private LoadMoreView loadMoreView;
    private View mTarget;

    private double mDragIndex = 1f;
    private final double mRatio = 400d;

    private int mAnimeDuration = 300;

    private boolean mRefreshing = false;

    private boolean mLoading = false;

    private boolean mAction = false;

    private boolean mLoadMoreEnable = false;

    private float mTouchY;

    private int mMode = -1;

    private final int MODE_REFRESH = 0;

    private final int MODE_LOADMORE = 1;

    public static final int STYLE_CLASSIC = 2;
    public static final int STYLE_GOOGLE = 3;

    private int mStyle = STYLE_CLASSIC;



    public RefreshLayout(Context context) {
        this(context,null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);



    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("QRefreshLayout can only have one child");
        }
        mTarget = getChildAt(0);
        if (refreshView == null) {
            createDefaultHeaderView();
        }
        if (loadMoreView == null) {
            createDefaultFooterView();
        }
    }

    public void setHeaderView(RefreshView view) {
        if (view == refreshView) return;
        if (refreshView != null) removeView(refreshView);
        refreshView = view;
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(refreshView, headerParams);
    }


    public void setFooterView(LoadMoreView view) {
        if (view == loadMoreView) return;
        if (loadMoreView != null) removeView(loadMoreView);
        loadMoreView = view;
        LayoutParams footerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        footerParams.gravity = Gravity.BOTTOM;
        addView(loadMoreView, footerParams);
    }


    public void setRefreshingHeight(int height) {
        mFinalHeight = height;
        mRefreshHeight = (int) (mFinalHeight * 1.3d);
    }

    private void createDefaultHeaderView() {
        setHeaderView(new RefreshView(getContext()));
    }

    private void createDefaultFooterView() {
        setFooterView(new LoadMoreView(getContext()));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (MotionEventCompat.getActionMasked(ev)){

            case  MotionEvent.ACTION_DOWN:{
                mTouchY = ev.getY();
                break;
            }

            case MotionEvent.ACTION_MOVE:{
                float currY = ev.getY();
                float diffy = currY-mTouchY;
                mTouchY = currY;

                if (diffy >0 && canTargetScrollUp() && !mAction){
                    mMode = MODE_REFRESH;
                }else if (diffy<0 && mLoadMoreEnable && !canTargetScrollDown()&& !mAction){
                    mMode = MODE_LOADMORE;
                }
                handleScroll(diffy);

                if (mStyle == STYLE_GOOGLE && (refreshView.getHeight()!=0 || loadMoreView.getHeight()!=0)){
                    return true;
                }

                break;
            }
            case MotionEvent.ACTION_UP:{
                onPointerUp();
                break;
            }

        }


        return super.dispatchTouchEvent(ev);
    }






    private boolean canTargetScrollUp() {

        if (mTarget == null){
            return false;
        }
        if (Build.VERSION.SDK_INT<14){
            if (mTarget instanceof AbsListView){
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount()>0 && (absListView.getFirstVisiblePosition()>0||absListView.getChildAt(0).getTop()<absListView.getPaddingTop());

            }else {
                return ViewCompat.canScrollVertically(mTarget,-1) || mTarget.getScaleY()>0;
            }
        }else {
            return ViewCompat.canScrollVertically(mTarget,-1);
        }

    }

    private boolean canTargetScrollDown() {
        if (mTarget == null)
            return false;
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() == absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1)
                        .getBottom() <= absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }


    private void handleScroll(float diffy) {
        if (mMode == MODE_REFRESH){
            mAction = true;
            handleHeaderScroll(diffy);
        }
        else if (mMode == MODE_LOADMORE) {
            mAction = true;
            handleFooterScroll(diffy);
        }

    }


    private void handleHeaderScroll(float diffy) {

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) refreshView.getLayoutParams();
        if (!canTargetScrollUp() && diffy>0){
            if (mRefreshing){
                params.height += diffy;

                if (params.height>mFinalHeight)
                    params.height = mFinalHeight;
            }else {
                mDragIndex = Math.exp(-(params.height/mRatio));
                if (mDragIndex<0)
                    mDragIndex =0;
                params.height += diffy*mDragIndex;
                if (params.height > mRefreshHeight){
                    syncLoadViewState(refreshView,refreshView.STATE_PULLING);
                }
            }

        }

        if (diffy<0){
            params.height += diffy;
            if (params.height < 0) {
                params.height = 0;
                syncLoadViewState(refreshView, refreshView.STATE_NORMAL);
                mAction = false;
            } else if (params.height < mRefreshHeight) {
                syncLoadViewState(refreshView, refreshView.STATE_NORMAL);
            }
        }
        refreshView.setLayoutParams(params);
        handleTargetHeight(params.height);
    }



    private void handleFooterScroll(float diffy) {
        LayoutParams params = (LayoutParams) loadMoreView.getLayoutParams();
        if (diffy < 0 && !canTargetScrollDown()) {
            if (mLoading) {
                params.height -= diffy;
                if (params.height > mFinalHeight) params.height = mFinalHeight;
            } else {
                mDragIndex = Math.exp(-(params.height / mRatio));
                if (mDragIndex < 0) mDragIndex = 0;
                params.height -= diffy * mDragIndex;
                if (params.height > mRefreshHeight) {
                    syncLoadViewState(loadMoreView, loadMoreView.STATE_PULLING);
                }
            }
        }

        if (diffy > 0) {
            params.height -= diffy;
            if (params.height < 0) {
                params.height = 0;
                syncLoadViewState(loadMoreView, loadMoreView.STATE_NORMAL);
                mAction = false;
            } else if (params.height < mRefreshHeight) {
                syncLoadViewState(loadMoreView, loadMoreView.STATE_NORMAL);
            }
        }
        loadMoreView.setLayoutParams(params);
        handleTargetHeight(-params.height);

    }

    private void syncLoadViewState(LoadView loadView, int state) {
        if ( (mRefreshing && loadView.equals(refreshView))  || (mLoading && loadView .equals(loadMoreView))){
            loadView.setRefreshing();
        }else if (state == LoadView.STATE_NORMAL){
            loadView.setNormal();
        }else if (state == loadView.STATE_PULLING){
            loadView.setPulling();
        }else if (state == loadView.STATE_REFRESH){
            loadView.setRefreshing();
            if (loadView.equals(refreshView)){
                mRefreshing = true;
                if (refreshAndLoadImp != null)
                    refreshAndLoadImp.onRefresh(this);
            }else if (loadView.equals(loadMoreView)){
                mLoading = true;
                if (refreshAndLoadImp !=null)
                    refreshAndLoadImp.onLoadMore(this);
            }
        }
    }

    private void handleTargetHeight(int height) {
        if (mStyle == STYLE_CLASSIC)
            mTarget.setTranslationY(height);
    }





    private void onPointerUp() {
        if (mMode == MODE_REFRESH) {
            onRefreshPointerUp();
        } else if (mMode == MODE_LOADMORE) {
            onLoadPointUp();
        }
    }
    private void onRefreshPointerUp() {
        if (!mRefreshing) onPointerUp(refreshView);
    }

    private void onLoadPointUp() {
        if (!mLoading) onPointerUp(loadMoreView);
    }



    private void onPointerUp(LoadView view) {
        int state = -1;
        int height = view.getHeight();
        if (height > mRefreshHeight) {
            height = mFinalHeight;
            state = LoadView.STATE_REFRESH;
        } else if (height < mRefreshHeight) {
            height = 0;
            state = LoadView.STATE_NORMAL;
        }
        startPullAnime(view, height, null);
        syncLoadViewState(view, state);
    }


    private void startPullAnime(final View view, int newHeight, Animator.AnimatorListener listener) {
        ValueAnimator anime = ValueAnimator.ofInt(view.getHeight(), newHeight);
        anime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = Integer.parseInt(animation.getAnimatedValue().toString());
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                params.height = h;
                view.setLayoutParams(params);
                if (view.equals(refreshView)) handleTargetHeight(h);
                else if (view.equals(loadMoreView)) handleTargetHeight(-h);

            }
        });
        if (listener != null)
            anime.addListener(listener);
        anime.setDuration(mAnimeDuration);
        anime.start();
    }



    public void setRefreshHandler(RefreshAndLoadImp refreshAndLoadImp) {
        this.refreshAndLoadImp = refreshAndLoadImp;
    }

    public void refreshBegin() {
        startPullAnime(refreshView, mFinalHeight, null);
        syncLoadViewState(refreshView, LoadView.STATE_REFRESH);
    }

    public void refreshComplete() {
        mRefreshing = false;
        refreshView.setComplete();
        startPullAnime(refreshView, 0, new AnimeListener(refreshView));
    }

    public void LoadMoreComplete() {
        mLoading = false;
        loadMoreView.setComplete();
        startPullAnime(loadMoreView, 0, new AnimeListener(loadMoreView));
        handleTargetBottom();
    }

    public void setLoadMoreEnable(boolean enable) {
        mLoadMoreEnable = enable;
    }




    public void setRefreshStyle(int style) {
        if (style != STYLE_CLASSIC && style != STYLE_GOOGLE)
            throw new IllegalStateException("not support style");
        mStyle = style;
    }

    private class AnimeListener extends AnimatorListenerAdapter {
        private LoadView mView;

        public AnimeListener(LoadView view) {
            mView = view;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mView != null) mView.setNormal();
        }
    }

    private void handleTargetBottom() {
        mTarget.post(new Runnable() {
            @Override
            public void run() {
                if (mTarget instanceof AbsListView) {
                    ((AbsListView) mTarget).setSelection(((AbsListView) mTarget).getAdapter().getCount() - 1);
                } else if (mTarget instanceof RecyclerView) {
                    ((RecyclerView) mTarget).scrollToPosition(((RecyclerView) mTarget).getAdapter().getItemCount() - 1);
                } else if (mTarget instanceof ScrollView) {
                    ((ScrollView) mTarget).fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }


}
