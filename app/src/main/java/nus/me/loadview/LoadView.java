package nus.me.loadview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by nus on 16-10-8.
 */
public abstract class LoadView extends ViewGroup {
    public static final int STATE_REFRESH = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_PULLING = 2;
    public static final int STATE_COMPLETE = 3;

    public LoadView(Context context) {
        super(context);
    }

    public LoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public abstract void setNormal();

    public abstract void setPulling();

    public abstract void setRefreshing();

    public abstract void setComplete();
}