package com.baidusoso.wifitransfer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements Animator.AnimatorListener {

    Unbinder mUnbinder;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.booklist)
    RecyclerView mBookList;

    List<String> mBooks = new ArrayList<>();
    BookshelfAdapter mBookshelfAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Timber.plant(new Timber.DebugTree());
        RxBus.get().register(this);
        initRecyclerView();
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
        WebService.stop(this);
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        RxBus.get().unregister(this);
    }

    @Subscribe(tags = {@Tag(Constants.RxBusEventType.POPUP_MENU_DIALOG_SHOW_DISMISS)})
    public void onPopupMenuDialogDismiss(Integer type) {
        if (type == Constants.MSG_DIALOG_DISMISS) {
            WebService.stop(this);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", mFab.getHeight() * 2, 0).setDuration(200L);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.start();
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        WebService.start(this);
        new PopupMenuDialog(this).builder().setCancelable(false)
                .setCanceledOnTouchOutside(false).show();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    void initRecyclerView() {
        mBookshelfAdapter = new BookshelfAdapter();
        mBookList.setHasFixedSize(true);
        mBookList.setLayoutManager(new GridLayoutManager(this, 3));
        mBookList.setAdapter(mBookshelfAdapter);
        RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
    }

    @Subscribe(thread = EventThread.IO, tags = {@Tag(Constants.RxBusEventType.LOAD_BOOK_LIST)})
    public void loadBookList(Integer type) {
        Timber.d("loadBookList:" + Thread.currentThread().getName());
        List<String> books = new ArrayList<>();
        File dir = Constants.DIR;
        if (dir.exists() && dir.isDirectory()) {
            String[] fileNames = dir.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    books.add(fileName);
                }
            }
        }
        runOnUiThread(() -> {
            mBooks.clear();
            mBooks.addAll(books);
            mBookshelfAdapter.notifyDataSetChanged();
        });
    }

    @Deprecated
    private void loadBookList() {
        Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                List<String> books = new ArrayList<>();
                File dir = Constants.DIR;
                if (dir.exists() && dir.isDirectory()) {
                    String[] fileNames = dir.list();
                    for (String fileName : fileNames) {
                        books.add(fileName);
                    }
                }
                subscriber.onNext(books);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<String>>() {
            @Override
            public void onCompleted() {
                mBookshelfAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                mBookshelfAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNext(List<String> books) {
                mBooks.clear();
                mBooks.addAll(books);
            }
        });
    }

    class BookshelfAdapter extends RecyclerView.Adapter<BookshelfAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    MainActivity.this).inflate(R.layout.layout_book_item, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.mTvBookName.setText(mBooks.get(position));
        }

        @Override
        public int getItemCount() {
            return mBooks.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mTvBookName;

            public MyViewHolder(View view) {
                super(view);
                mTvBookName = (TextView) view.findViewById(R.id.book_name);
            }
        }
    }
}
