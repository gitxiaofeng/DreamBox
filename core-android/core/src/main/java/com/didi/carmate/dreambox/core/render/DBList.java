package com.didi.carmate.dreambox.core.render;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.didi.carmate.dreambox.core.action.IDBAction;
import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.render.view.list.AdapterCallback;
import com.didi.carmate.dreambox.core.render.view.list.DBListAdapter;
import com.didi.carmate.dreambox.core.render.view.list.DBListInnerAdapter;
import com.didi.carmate.dreambox.core.render.view.list.DBListItemRoot;
import com.didi.carmate.dreambox.core.render.view.list.DBListView;
import com.didi.carmate.dreambox.core.render.view.list.IAdapterCallback;
import com.didi.carmate.dreambox.core.render.view.list.IRefreshListener;
import com.didi.carmate.dreambox.core.render.view.list.OnLoadMoreListener;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.core.utils.DBThreadUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/5/11
 */
public class DBList extends DBBaseView<DBListView> {
    private static final String TAG = "DBList";

    private String rawSrc;
    private String orientation = DBConstants.LIST_ORIENTATION_V;
    private List<JsonObject> src;
    private boolean pullRefresh;
    private boolean loadMore;
    private int pageIndex;
    private int pageSize;
    private int pageCount;
    private int pageNext;
    private DBListInnerAdapter mInnerAdapter;
    private DBListAdapter mAdapter;

    private IDBRecycleRender mDBListHeader;
    private IDBRecycleRender mDBListFooter;
    private IDBRecycleRender mDBListVh;

    @DBDomAttr(key = "orientation")
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    @DBDomAttr(key = "src")
    public void setRawSrc(String rawSrc) {
        this.rawSrc = rawSrc;
    }

    @DBDomAttr(key = "pullRefresh")
    public void setPullRefresh(String pullRefresh) {
        if (DBUtils.isEmpty(pullRefresh)) {
            this.pullRefresh = false;
        } else {
            this.pullRefresh = "true".equals(pullRefresh);
        }
    }

    @DBDomAttr(key = "loadMore")
    public void setLoadMore(String loadMore) {
        if (DBUtils.isEmpty(loadMore)) {
            this.loadMore = false;
        } else {
            this.loadMore = "true".equals(loadMore);
        }
    }

    @DBDomAttr(key = "pageIndex")
    public void setPageIndex(String pageIndex) {
        if (DBUtils.isEmpty(pageIndex)) {
            this.pageIndex = 0;
        } else {
            this.pageIndex = DBUtils.isNumeric(pageIndex) ? Integer.parseInt(pageIndex) : 0;
        }
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        // header/footer预处理
        List<IDBNode> childNodes = getChildren();
        if (null != childNodes) {
            for (IDBNode node : childNodes) {
                if (node instanceof DBListHeader) {
                    mDBListHeader = (DBListHeader) node;
                } else if (node instanceof DBListFooter) {
                    mDBListFooter = (DBListFooter) node;
                } else if (node instanceof DBListVh) {
                    mDBListVh = (DBListVh) node;
                }
            }
        } else {
            Wrapper.get(mDBContext.getAccessKey()).log().i(getClass().getSimpleName()
                    + "[DBChildren] node is null or [DBChildren] node has no child.");
        }
    }

    @Override
    public boolean processChildAttr() {
        return false; // 截断子节点树形遍历逻辑，在mDBListHeader/mDBListFooter/mDBListVh里触发
    }

    @Override
    public void processAttr() {
        super.processAttr();

        src = getVariableList(rawSrc);
        // 因为数据源需要从各个Item里获取，所以Item子节点属性处理在Adapter的[onBindItemView]回调里处理
    }

    @Override
    protected DBListView createView(DBRootView parentView) {
        return new DBListView(mDBContext);
    }

    @Override
    public void doRender(final DBListView recyclerView) {
        super.doRender(recyclerView);

        mInnerAdapter = new DBListInnerAdapter(mDBContext, src, adapterCallback, orientation);
        mAdapter = new DBListAdapter(mInnerAdapter, adapterCallback, orientation,
                mDBListHeader != null, mDBListFooter != null);
        recyclerView.setAdapter(mAdapter);

        if (pullRefresh || loadMore) {
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            recyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        }

        LinearLayoutManager managerVertical = new LinearLayoutManager(mDBContext.getContext());
        if (orientation.equals(DBConstants.LIST_ORIENTATION_H)) {
            managerVertical.setOrientation(LinearLayoutManager.HORIZONTAL);
        } else {
            managerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setLayoutManager(managerVertical);

        // 下拉动作事件触发
        recyclerView.setOnRefreshListener(new IRefreshListener() {
            @Override
            public void onRefresh() {
                List<IDBAction> actionList = mDBActionPool.getListOnPullAction();
                if (null != actionList) {
                    for (IDBAction action : actionList) {
                        action.invoke();
                    }
                }
                // FIXME mock net request
                DBThreadUtils.runOnMain(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.refreshComplete(loadMore ? pageSize : src.size());
                    }
                }, 2000);
            }
        });

        recyclerView.setPullRefreshEnabled(pullRefresh);
        recyclerView.setLoadMoreEnabled(loadMore);
        recyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
//                    if (mCurrentCounter < TOTAL_COUNTER) {
//                        // loading more
//                        requestData();
//                    } else {
//                        //the end
//                        recyclerView.setNoMore(true);
//                    }
                recyclerView.setNoMore(true);
            }
        });

        // add a HeaderView or FooterView
        if (null != mDBListHeader) {
            mAdapter.addHeaderView(new DBRootView(mDBContext));
        }
        // footer view 必须在setLoadMoreEnabled后面调用，否则会被删除掉
        if (null != mDBListFooter) {
            mAdapter.addFooterView(new DBRootView(mDBContext));
        }

        // 子节点渲染处理在Adapter的[onBindViewHolder]回调里处理
    }

    @Override
    public void changeOnCallback(DBListView listView, String key, Object oldValue, Object newValue) {
        if (null != newValue && null != mInnerAdapter) {
            src = getVariableList(rawSrc);
            mInnerAdapter.setData(src);
        }
    }

    private IAdapterCallback adapterCallback = new AdapterCallback() {
        @Override
        public void onBindHeaderView(DBRootView rootView) {
            // header节点渲染处理
            if (null != mDBListHeader) {
                // 子节点属性处理
                mDBListHeader.processAttr();
                if (rootView.getChildCount() == 0) {
                    mDBListHeader.processRender(rootView);
                    rootView.onRenderFinish(mDBListHeader);
                } else {
                    mDBListHeader.recycleRender(rootView);
                }
            }
        }

        @Override
        public void onBindFooterView(DBRootView rootView) {
            // footer子节点渲染处理
            if (null != mDBListFooter) {
                // 子节点属性处理
                mDBListFooter.processAttr();
                if (rootView.getChildCount() == 0) {
                    mDBListFooter.processRender(rootView);
                    rootView.onRenderFinish(mDBListFooter);
                } else {
                    mDBListFooter.recycleRender(rootView);
                }
            }
        }

        @Override
        public void onBindItemView(DBListItemRoot itemRoot, JsonObject data) {
            if (null != mDBListVh) {
                // 子节点属性处理
                mDBListVh.processAttr(data);
                if (itemRoot.getChildCount() == 0) {
                    // 子节点渲染处理
                    mDBListVh.processRender(itemRoot);
                    itemRoot.onRenderFinish(mDBListVh);
                } else {
                    mDBListVh.recycleRender(itemRoot);
                }
            }
        }
    };

    public static String getNodeTag() {
        return "list";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBList createNode() {
            return new DBList();
        }
    }
}
