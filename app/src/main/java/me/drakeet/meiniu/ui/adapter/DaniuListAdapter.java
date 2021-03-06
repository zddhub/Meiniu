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

package me.drakeet.meiniu.ui.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.meiniu.R;
import me.drakeet.meiniu.model.Daniu;
import me.drakeet.meiniu.ui.WebActivity;
import me.drakeet.meiniu.util.StringStyleUtils;

/**
 * Created by drakeet on 8/11/15.
 */
public class DaniuListAdapter extends AnimRecyclerViewAdapter<DaniuListAdapter.ViewHolder> {

    private List<Daniu> mDaniuList;


    public DaniuListAdapter(List<Daniu> daniuList) {
        mDaniuList = daniuList;
    }


    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daniu, parent, false);
        return new ViewHolder(v);
    }


    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        Daniu daniu = mDaniuList.get(position);
        if (position == 0) {
            showCategory(holder);
        }
        else {
            boolean theCategoryOfLastEqualsToThis =
                    mDaniuList.get(position - 1).type.equals(mDaniuList.get(position).type);
            if (!theCategoryOfLastEqualsToThis) {
                showCategory(holder);
            }
            else {
                hideCategory(holder);
            }
        }
        holder.category.setText(daniu.type);
        SpannableStringBuilder builder = new SpannableStringBuilder(daniu.desc).append(
                StringStyleUtils.format(holder.gank.getContext(), " (via. " + daniu.who + ")",
                        R.style.ViaTextAppearance));
        CharSequence gankText = builder.subSequence(0, builder.length());

        holder.gank.setText(gankText);
        showItemAnim(holder.gank, position);
    }


    @Override public int getItemCount() {
        return mDaniuList.size();
    }


    private void showCategory(ViewHolder holder) {
        if (!isVisibleOf(holder.category)) holder.category.setVisibility(View.VISIBLE);
    }


    private void hideCategory(ViewHolder holder) {
        if (isVisibleOf(holder.category)) holder.category.setVisibility(View.GONE);
    }


    /**
     * view.isShown() is a kidding...
     */
    private boolean isVisibleOf(View view) {
        return view.getVisibility() == View.VISIBLE;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_category) TextView category;
        @Bind(R.id.tv_title) TextView gank;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        @OnClick(R.id.ll_gank_parent) void onGank(View v) {
            Daniu daniu = mDaniuList.get(getLayoutPosition());
            Intent intent = WebActivity.newIntent(v.getContext(), daniu.url, daniu.desc);
            v.getContext().startActivity(intent);
        }
    }
}
