package com.didi.carmate.dreambox.core.render.view.list;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/7/1
 */
public class DBListInnerAdapter extends RecyclerView.Adapter<DBListViewHolder> {
    private DBContext mDBContext;
    private IAdapterCallback mAdapterCallback;
    private List<JsonObject> mListData;
    private String mOrientation;
    private int mParentWidth;
    private int mParentHeight;

    public DBListInnerAdapter(DBContext dbContext, List<JsonObject> listData, IAdapterCallback innerAdapterCallback,
                              String orientation) {
        mDBContext = dbContext;
        mListData = listData;
        mAdapterCallback = innerAdapterCallback;
        mOrientation = orientation;
    }

    public void setData(List<JsonObject> listData) {
        mListData = listData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DBListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DBListItemRoot itemRoot = new DBListItemRoot(mDBContext);
        ViewGroup.LayoutParams lp;
        if (mOrientation.equals(DBConstants.LIST_ORIENTATION_H)) {
            if (mParentHeight == 0) {
                mParentHeight = ((DBListView) parent).getLayoutManager().getHeight();
            }
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mParentHeight);
        } else {
            if (mParentWidth == 0) {
                mParentWidth = ((DBListView) parent).getLayoutManager().getWidth();
            }
            lp = new ViewGroup.LayoutParams(mParentWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        itemRoot.setLayoutParams(lp);
        return new DBListViewHolder(itemRoot);
    }

    @Override
    public void onBindViewHolder(@NonNull DBListViewHolder holder, int position) {
        if (null != mAdapterCallback) {
            mAdapterCallback.onBindItemView(holder.getListItemRoot(), mListData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }
}
