package moe.feng.media.cleaninvalid.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import moe.feng.media.cleaninvalid.R;

/**
 * Control the elevation of raised view by recycler view (layout manager)
 *
 * @author Fung Gwo (fython@163.com)
 */
public class RaisedViewScrollListener extends RecyclerView.OnScrollListener {

    @NonNull
    public View mTargetView;

    @NonNull
    private Map<Boolean, Float> mStatedElevation = new HashMap<>();
    private long mDuration;

    @Nullable
    private Animator mElevationAnimator = null;
    @Nullable
    private Boolean mAnimatorDirection = null;

    public RaisedViewScrollListener(@NonNull View targetView) {
        mTargetView = Objects.requireNonNull(targetView);

        final Context context = targetView.getContext();

        mStatedElevation.put(true, context.getResources().getDimension(R.dimen.toolbar_elevation));
        mStatedElevation.put(false, 0F);

        mDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    private float getElevationInState(boolean state) {
        return Objects.requireNonNull(mStatedElevation.get(state));
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        final LinearLayoutManager layoutManager;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        } else {
            throw new IllegalArgumentException(
                    RaisedViewScrollListener.class.getSimpleName()
                    + " currently supports LinearLayoutManager only."
            );
        }
        final boolean shouldRaise = layoutManager.findFirstCompletelyVisibleItemPosition() > 0;

        if (mAnimatorDirection == null || mAnimatorDirection != shouldRaise) {
            if (mElevationAnimator != null && mElevationAnimator.isRunning()) {
                mElevationAnimator.cancel();
            }
        }
        mAnimatorDirection = shouldRaise;
        if (mElevationAnimator == null || !mElevationAnimator.isRunning()) {
            if (mTargetView.getElevation() != getElevationInState(shouldRaise)) {
                mElevationAnimator = ObjectAnimator.ofFloat(
                        mTargetView,
                        "elevation",
                        getElevationInState(!shouldRaise),
                        getElevationInState(shouldRaise)
                ).setDuration(mDuration);
                mElevationAnimator.start();
            }
        }
    }

}
