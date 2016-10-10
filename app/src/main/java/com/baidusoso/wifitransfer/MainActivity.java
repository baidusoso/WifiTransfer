package com.baidusoso.wifitransfer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements Animator.AnimatorListener {

    Unbinder mUnbinder;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Timber.plant(new Timber.DebugTree());
        RxBus.get().register(this);
    }

    @OnClick(R.id.fab)
    public void onClick(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", 0, mFab.getHeight() * 2).setDuration(200L);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addListener(this);
        objectAnimator.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        RxBus.get().unregister(this);
    }

    @Subscribe(tags = {@Tag(Constants.RxBusEventType.POPUP_MENU_DIALOG_SHOW_DISMISS)})
    public void onPopupMenuDialogDismiss(Integer type) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", mFab.getHeight() * 2, 0).setDuration(200L);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.start();
    }

    @Override
    public void onAnimationStart(Animator animation) {
        Timber.d("onAnimationStart:%s", Thread.currentThread().getName());
        new PopupMenuDialog(this).builder().setCancelable(false)
                .setCanceledOnTouchOutside(false).show();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        Timber.d("onAnimationEnd:%s", Thread.currentThread().getName());
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        Timber.d("onAnimationCancel:%s", Thread.currentThread().getName());
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        Timber.d("onAnimationRepeat:%s", Thread.currentThread().getName());
    }
}
