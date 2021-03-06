/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.drakeet.meiniu.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.meiniu.data.DaniuData;
import me.drakeet.meiniu.event.OnKeyBackClickEvent;
import me.drakeet.meiniu.model.Daniu;
import me.drakeet.meiniu.ui.adapter.DaniuListAdapter;
import me.drakeet.meiniu.ui.base.BaseActivity;
import me.drakeet.meiniu.util.Once;
import me.drakeet.meiniu.util.ToastUtils;
import me.drakeet.meiniu.widget.VideoImageView;
import me.drakeet.meiniu.LoveBus;
import me.drakeet.meiniu.R;
import me.drakeet.meiniu.util.ShareUtils;
import me.drakeet.meiniu.widget.LoveVideoView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by drakeet on 8/11/15.
 */
public class DaniuFragment extends Fragment {

    private final String TAG = "DaniuFragment";
    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    @Bind(R.id.rv_gank) RecyclerView mRecyclerView;
    @Bind(R.id.stub_empty_view) ViewStub mEmptyViewStub;
    @Bind(R.id.stub_video_view) ViewStub mVideoViewStub;
    @Bind(R.id.iv_video)
    VideoImageView mVideoImageView;
    LoveVideoView mVideoView;

    int mYear, mMonth, mDay;
    List<Daniu> mDaniuList;
    String mVideoPreviewUrl;
    boolean mIsVideoViewInflated = false;
    Subscription mSubscription;
    DaniuListAdapter mAdapter;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DaniuFragment newInstance(int year, int month, int day) {
        DaniuFragment fragment = new DaniuFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }


    public DaniuFragment() {
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDaniuList = new ArrayList<>();
        mAdapter = new DaniuListAdapter(mDaniuList);
        parseArguments();
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    private void parseArguments() {
        Bundle bundle = getArguments();
        mYear = bundle.getInt(ARG_YEAR);
        mMonth = bundle.getInt(ARG_MONTH);
        mDay = bundle.getInt(ARG_DAY);
    }


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_daniu, container, false);
        ButterKnife.bind(this, rootView);
        initRecyclerView();
        setVideoViewPosition(getResources().getConfiguration());
        return rootView;
    }


    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mDaniuList.size() == 0) loadData();
    }


    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void loadData() {
        mSubscription = BaseActivity.sDrakeet.getGankData(mYear, mMonth, mDay)
                .map(data -> data.results)
                .map(this::addAllResults)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) { showEmptyView(); }
                    else {mAdapter.notifyDataSetChanged();}
                }, Throwable::printStackTrace);
    }


    private void loadVideoPreview() {
        if (mDaniuList.size() > 0 && mDaniuList.get(0).url != null) {
            String url = mDaniuList.get(0).url;
            if (url.startsWith("http://v.youku.com/")) {
                String id = url.substring(url.indexOf("id_")+3, url.indexOf(".html"));
                mVideoPreviewUrl = "http://events.youku.com/global/api/video-thumb.php?vid=" + id;
            }
        }
        if (mVideoPreviewUrl != null) {
            mVideoImageView.post(() -> Glide.with(mVideoImageView.getContext())
                    .load(mVideoPreviewUrl)
                    .into(mVideoImageView));
        }
    }

    private void startPreview(String preview) {
        mVideoPreviewUrl = preview;
        if (preview != null && mVideoImageView != null) {
            mVideoImageView.post(() -> Glide.with(mVideoImageView.getContext())
                    .load(preview)
                    .into(mVideoImageView));
        }
    }


    private void showEmptyView() {mEmptyViewStub.inflate();}


    private List<Daniu> addAllResults(DaniuData.Result results) {
        if (results.goList != null) mDaniuList.addAll(results.goList);
        if (results.swiftList != null) mDaniuList.addAll(results.swiftList);
        if (results.拓展资源List != null) mDaniuList.addAll(results.拓展资源List);
        if (results.实用工具List != null) mDaniuList.addAll(results.实用工具List);
        if (results.牛人设计List != null) mDaniuList.addAll(results.牛人设计List);
        if (results.牛人轶事List != null) mDaniuList.addAll(results.牛人轶事List);
        if (results.网络安全List != null) mDaniuList.addAll(results.网络安全List);
        if (results.搞笑视频List != null) mDaniuList.addAll(0, results.搞笑视频List);
        if (results.今日视频List != null) mDaniuList.addAll(0, results.今日视频List);

        loadVideoPreview();

        return mDaniuList;
    }


    @OnClick(R.id.header_appbar) void onPlayVideo() {
        resumeVideoView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mDaniuList.size() > 0 && (mDaniuList.get(0).type.equals("搞笑视频") || mDaniuList.get(0).type.equals("今日视频"))) {
            ToastUtils.showLongLong(R.string.loading);
        }
        else {
            closePlayer();
        }
    }


    private void setVideoViewPosition(Configuration newConfig) {
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: {
                if (mIsVideoViewInflated) {
                    mVideoViewStub.setVisibility(View.VISIBLE);
                }
                else {
                    mVideoView = (LoveVideoView) mVideoViewStub.inflate();
                    mIsVideoViewInflated = true;
                    String tip = getString(R.string.tip_video_play);
                    new Once(mVideoView.getContext()).show(tip,
                            () -> Snackbar.make(mVideoView, tip, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.i_know, v -> {})
                                    .show());
                }
                if (mDaniuList.size() > 0 && (mDaniuList.get(0).type.equals("今日视频") || mDaniuList.get(0).type.equals("搞笑视频"))) {
                    mVideoView.loadUrl(mDaniuList.get(0).url);
                }
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_UNDEFINED:
            default: {
                mVideoViewStub.setVisibility(View.GONE);
                break;
            }
        }
    }


    void closePlayer() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ToastUtils.showShort(getString(R.string.tip_for_no_daniu));
    }


    @Override public void onConfigurationChanged(Configuration newConfig) {
        setVideoViewPosition(newConfig);
        super.onConfigurationChanged(newConfig);
    }


    @Subscribe public void onKeyBackClick(OnKeyBackClickEvent event) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        clearVideoView();
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                if (mDaniuList.size() != 0) {
                    Daniu daniu = mDaniuList.get(0);
                    String shareText = daniu.desc + daniu.url + getString(R.string.share_from);
                    ShareUtils.share(getActivity(), shareText);
                }
                else {
                    ShareUtils.share(getActivity());
                }
                return true;
            case R.id.action_subject:
                openTodaySubject();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openTodaySubject() {
        String url =
                getString(R.string.url_daniu_io) + String.format("%s/%s/%s", mYear, mMonth, mDay);
        Intent intent = WebActivity.newIntent(getActivity(), url, getString(R.string.action_subject));
        startActivity(intent);
    }


    @Override public void onResume() {
        super.onResume();
        LoveBus.getLovelySeat().register(this);
        resumeVideoView();
    }


    @Override public void onPause() {
        super.onPause();
        LoveBus.getLovelySeat().unregister(this);
        pauseVideoView();
        clearVideoView();
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) mSubscription.unsubscribe();
        resumeVideoView();
    }


    private void pauseVideoView() {
        // oh, my egg
        if (mVideoView != null) {
            mVideoView.onPause();
            mVideoView.pauseTimers();
        }
    }


    private void resumeVideoView() {
        // egg pain
        if (mVideoView != null) {
            mVideoView.resumeTimers();
            mVideoView.onResume();
        }
    }


    private void clearVideoView() {
        if (mVideoView != null) {
            mVideoView.clearHistory();
            mVideoView.clearCache(true);
            mVideoView.loadUrl("about:blank");
            mVideoView.pauseTimers();
        }
    }
}
