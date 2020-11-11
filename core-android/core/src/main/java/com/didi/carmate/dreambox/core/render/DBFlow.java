package com.didi.carmate.dreambox.core.render;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.render.view.flow.DBFlowAdapter;
import com.didi.carmate.dreambox.core.render.view.flow.DBFlowLayout;
import com.didi.carmate.dreambox.core.render.view.list.AdapterCallback;
import com.didi.carmate.dreambox.core.render.view.list.IAdapterCallback;
import com.didi.carmate.dreambox.core.utils.DBScreenUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * author: chenjing
 * date: 2020/5/11
 */
public class DBFlow extends DBBaseView<DBFlowLayout> {
    private String rawSrc;
    private String rawHSpace;
    private String rawVSpace;
    private List<JsonObject> src;
    private int hSpace;
    private int vSpace;

    private DBFlowAdapter<JsonObject> mAdapter;

    private IDBRender mDBChildren;

    @DBDomAttr(key = "src")
    public void setRawSrc(String rawSrc) {
        this.rawSrc = rawSrc;
    }

    @DBDomAttr(key = "hSpace")
    public void setRawHSpace(String rawHSpace) {
        this.rawHSpace = rawHSpace;
    }

    @DBDomAttr(key = "vSpace")
    public void setRawVSpace(String rawVSpace) {
        this.rawVSpace = rawVSpace;
    }

    @Override
    public boolean preProcessChild() {
        return false; // 截断子节点树形遍历逻辑
    }

    @Override
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        // 子节点预处理
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            for (IDBNode node : childNodes) {
                if (node instanceof DBChildren) {
                    mDBChildren = (DBChildren) node;
                }
                node.preProcess(mDBContext);
            }
        } else {
            Wrapper.get(mDBContext.getAccessKey()).log().e("flow child can not be null.");
        }
    }

    @Override
    public boolean processChildAttr() {
        return false; // 截断子节点树形遍历逻辑，会在 mDBChildren 里重新触发
    }

    @Override
    public void processAttr() {
        super.processAttr();

        src = getVariableList(rawSrc);
        hSpace = DBScreenUtils.processSize(mDBContext, rawHSpace, 0);
        vSpace = DBScreenUtils.processSize(mDBContext, rawVSpace, 0);
        // 因为数据源需要从各个Item里获取，所以Item子节点属性处理在Adapter的[onBindItemView]回调里处理
    }

    @Override
    protected DBFlowLayout createView(DBRootView parentView) {
        return new DBFlowLayout(mDBContext);
    }

    @Override
    protected void doRender(DBFlowLayout flowLayout) {
        super.doRender(flowLayout);

        mAdapter = new DBFlowAdapter<>(mDBContext, flowLayout, src, adapterCallback);
        flowLayout.setAdapter(mAdapter);
        flowLayout.setChildSpacing(hSpace);
        flowLayout.setRowSpacing(vSpace);

        // 子节点渲染处理在Adapter的[onBindViewHolder]回调里处理
    }

    @Override
    public void changeOnCallback(DBFlowLayout flowLayout, String key, Object oldValue, Object newValue) {
        if (null != newValue && null != mAdapter) {
            src = getVariableList(rawSrc);
            mAdapter.setData(src);
        }
    }

    private IAdapterCallback adapterCallback = new AdapterCallback() {
        @Override
        public void onBindItemView(DBRootView itemRoot, JsonObject data) {
            if (null == mDBChildren) {
                Wrapper.get(mDBContext.getAccessKey()).log().e("flow child can not be null.");
                return;
            }

            // 子节点属性处理
            mDBChildren.processAttr(data);
            // 子节点渲染处理
            mDBChildren.processRender(itemRoot);
            itemRoot.onRenderFinish(mDBChildren);
        }
    };

    public static String getNodeTag() {
        return "flow";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBFlow createNode() {
            return new DBFlow();
        }
    }
}
