package com.didi.carmate.dreambox.core.render;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.didi.carmate.dreambox.core.action.DBAlias;
import com.didi.carmate.dreambox.core.action.IDBAction;
import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBDomAttr;
import com.didi.carmate.dreambox.core.base.DBNode;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.bridge.DBOnEvent;
import com.didi.carmate.dreambox.core.data.DBData;
import com.didi.carmate.dreambox.core.data.DBMeta;
import com.didi.carmate.dreambox.core.render.view.DBCoreViewH;
import com.didi.carmate.dreambox.core.render.view.DBCoreViewV;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.render.view.DreamBoxCoreView;
import com.didi.carmate.dreambox.core.render.view.IDBCoreView;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.core.utils.DBThreadUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: chenjing
 * date: 2020/4/30
 */
public class DBTemplate extends DBNode implements LifecycleObserver {
    private static final String TAG = "DBTemplate";

    private String xmlns;
    private String xsi;
    // 根节点id默认0，其他视图节点id默认值-1
    private int id = DBConstants.DEFAULT_ID_ROOT;
    private String changeOn;
    private String dismissOn;
    private String scroll;

    private IDBCoreView mDBCoreView;
    private DreamBoxCoreView mDreamBoxCoreView;
    private DBRootView mDBRootView;
    private String accessKey;
    private String templateId;

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        List<IDBAction> actionList = mDBActionPool.getVisibleAction();
        if (null != actionList) {
            for (IDBAction action : actionList) {
                action.invoke();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        List<IDBAction> actionList = mDBActionPool.getInvisibleAction();
        if (null != actionList) {
            for (IDBAction action : actionList) {
                action.invoke();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        // 资源释放
        release();
    }

    @DBDomAttr(key = "changeOn")
    public void setChangeOn(String changeOn) {
        this.changeOn = changeOn;
    }

    @DBDomAttr(key = "dismissOn")
    public void setDismissOn(String dismissOn) {
        this.dismissOn = dismissOn;
    }

    @DBDomAttr(key = "xmlns:xsi")
    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    @DBDomAttr(key = "xsi:noNamespaceSchemaLocation")
    public void setXsi(String xsi) {
        this.xsi = xsi;
    }

    @DBDomAttr(key = "scroll")
    public void setScroll(String scroll) {
        this.scroll = scroll;
    }

    @Override
    public void release() {
        mDBActionPool.release();

        // 子节点资源释放
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            for (IDBNode node : childNodes) {
                node.release();
            }
        }

        // 释放Activity
        if (mDBContext != null && null != mDBContext.getLifecycle()) {
            mDBContext.getLifecycle().removeObserver(this);
            mDBContext.release();
            mDBContext = null;
        }
    }

    public void onEvent() {
        List<IDBAction> actionList = mDBActionPool.getBridgeOnEventAction();
        if (null != actionList) {
            for (IDBAction action : actionList) {
                action.invoke();
            }
        }
    }

    /**
     * 绑定承载模板对象Activity的生命周期
     */
    public void bindLifecycle(final Lifecycle lifecycle) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            lifecycle.addObserver(this);
        } else {
            DBThreadUtils.runOnMain(new Runnable() {
                @Override
                public void run() {
                    lifecycle.addObserver(DBTemplate.this);
                }
            });
        }
    }

    /**
     * 重新刷新整个模板
     */
    public void invalidate() {
        processAttr();
        processRender();
        processAction();
    }

    public IDBCoreView getDreamBoxCoreView() {
        return mDBCoreView;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public DBMeta getDBMeta() {
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            // 拿到Render节点
            for (IDBNode node : childNodes) {
                if (node instanceof DBMeta) {
                    return (DBMeta) node;
                }
            }
        }
        return null;
    }

    public Map<String, DBAlias> getDBAlias() {
        Map<String, DBAlias> dbAliasMap = new HashMap<>();
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            // 拿到 [action-alias] 节点
            for (IDBNode node : childNodes) {
                if (node instanceof DBAlias) {
                    DBAlias dbAlias = (DBAlias) node;
                    dbAliasMap.put(dbAlias.getId(), dbAlias);
                }
            }
        }
        return dbAliasMap;
    }

    public DBOnEvent getDBOnEvent() {
        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            // 拿OnEvent事件节点
            for (IDBNode node : childNodes) {
                if (node instanceof DBOnEvent) {
                    return (DBOnEvent) node;
                }
            }
        }
        return null;
    }

    /**
     * 预处理、初始化等相关动作
     */
    public void preProcess(DBContext dbContext) {
        super.preProcess(dbContext);

        // 构建根节点本地对象
        if (!DBUtils.isEmpty(scroll) && null == mDBRootView) {
            mDBRootView = new DBRootView(dbContext);
        }
        if (null == mDBCoreView) {
            if (DBConstants.STYLE_ORIENTATION_V.equals(scroll)) {
                mDBCoreView = new DBCoreViewV(dbContext, mDBRootView);
            } else if (DBConstants.STYLE_ORIENTATION_H.equals(scroll)) {
                mDBCoreView = new DBCoreViewH(dbContext, mDBRootView);
            } else {
                mDBCoreView = mDreamBoxCoreView = new DreamBoxCoreView(dbContext);
            }
        }
    }

    /**
     * 预处理，循环调用所有子节点的预处理接口
     */
    @Override
    public void processAttr() {
        super.processAttr();

        // observe meta [dismissOn]
        if (null != dismissOn) {
            mDBContext.observeData(new DBData.IDBObserveData<Boolean>() {
                @Override
                public void onDataChanged(DBTemplate template, String key, @Nullable Boolean oldValue, @NonNull Boolean newValue) {
                    Wrapper.get(mDBContext.getAccessKey()).log().d("key: " + key + " oldValue: " + oldValue + " newValue: " + newValue);
                    if (null != mDBCoreView) {
                        mDBCoreView.getView().setVisibility(newValue ? View.GONE : View.VISIBLE);
                    }
                }

                @Override
                public String getDataKey() {
                    return dismissOn;
                }
            });
        }

        // observe meta [changeOn]
        if (null != changeOn) {
            String[] keys = changeOn.split("\\|");
            for (final String key : keys) {
                mDBContext.observeData(new DBData.IDBObserveData<Object>() {
                    @Override
                    public void onDataChanged(DBTemplate template, String key, @Nullable Object oldValue, @NonNull Object newValue) {
//                    Log.d(TAG, "key: " + key + " oldValue: " + oldValue + " newValue: " + newValue);
                        invalidate();
                    }

                    @Override
                    public String getDataKey() {
                        return key;
                    }
                });
            }
        }
    }

    /**
     * 执行渲染
     */
    public void processRender() {
        // 处理根节点渲染
        doProcessRender();

        List<IDBNode> childNodes = getChildNodes();
        if (null != childNodes && childNodes.size() > 0) {
            for (IDBNode node : childNodes) {
                if (node instanceof IDBRender) {
                    IDBRender dbRender = (IDBRender) node;
                    if (DBConstants.STYLE_ORIENTATION_V.equals(scroll) || DBConstants.STYLE_ORIENTATION_H.equals(scroll)) {
                        dbRender.processRender(mDBRootView);
                        mDBRootView.onRenderFinish(dbRender);
                    } else {
                        dbRender.processRender(mDreamBoxCoreView);
                        mDreamBoxCoreView.onRenderFinish(dbRender);
                    }
                    break;
                }
            }
        }
    }

    private void doProcessRender() {
        View view = mDBCoreView.getView();
        if (null == view.getLayoutParams()) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
        }

        // dismissOn
        if (null == dismissOn || mDBContext.getBooleanValue(dismissOn)) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public static String getNodeTag() {
        return "dbl";
    }

    public static class NodeCreator implements INodeCreator {
        @Override
        public DBTemplate createNode() {
            return new DBTemplate();
        }
    }
}
