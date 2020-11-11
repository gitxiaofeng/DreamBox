package com.didi.carmate.dreambox.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.Lifecycle;

import com.didi.carmate.dreambox.core.action.DBActions;
import com.didi.carmate.dreambox.core.action.DBAlias;
import com.didi.carmate.dreambox.core.action.DBStorage;
import com.didi.carmate.dreambox.core.action.DBChangeMeta;
import com.didi.carmate.dreambox.core.action.DBClosePage;
import com.didi.carmate.dreambox.core.action.DBDialog;
import com.didi.carmate.dreambox.core.action.DBInvoke;
import com.didi.carmate.dreambox.core.action.DBLog;
import com.didi.carmate.dreambox.core.action.DBNav;
import com.didi.carmate.dreambox.core.action.DBNet;
import com.didi.carmate.dreambox.core.action.DBToast;
import com.didi.carmate.dreambox.core.action.DBTrace;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.DBNodeParser;
import com.didi.carmate.dreambox.core.base.DBNodeRegistry;
import com.didi.carmate.dreambox.core.base.INodeCreator;
import com.didi.carmate.dreambox.core.bridge.DBBridgeDBToNativeCallback;
import com.didi.carmate.dreambox.core.bridge.DBOnEvent;
import com.didi.carmate.dreambox.core.bridge.DBSendEvent;
import com.didi.carmate.dreambox.core.data.DBGlobalPool;
import com.didi.carmate.dreambox.core.data.DBMeta;
import com.didi.carmate.dreambox.core.callback.DBClick;
import com.didi.carmate.dreambox.core.callback.DBInVisible;
import com.didi.carmate.dreambox.core.callback.DBOnError;
import com.didi.carmate.dreambox.core.callback.DBOnNegative;
import com.didi.carmate.dreambox.core.callback.DBOnPositive;
import com.didi.carmate.dreambox.core.callback.DBOnSuccess;
import com.didi.carmate.dreambox.core.callback.DBVisible;
import com.didi.carmate.dreambox.core.render.DBButton;
import com.didi.carmate.dreambox.core.render.DBChildren;
import com.didi.carmate.dreambox.core.render.DBFlow;
import com.didi.carmate.dreambox.core.render.DBImage;
import com.didi.carmate.dreambox.core.render.DBList;
import com.didi.carmate.dreambox.core.render.DBListFooter;
import com.didi.carmate.dreambox.core.render.DBListHeader;
import com.didi.carmate.dreambox.core.render.DBListVh;
import com.didi.carmate.dreambox.core.render.DBLoading;
import com.didi.carmate.dreambox.core.render.DBProgress;
import com.didi.carmate.dreambox.core.render.DBRender;
import com.didi.carmate.dreambox.core.render.DBTemplate;
import com.didi.carmate.dreambox.core.render.DBText;
import com.didi.carmate.dreambox.core.render.DBView;
import com.didi.carmate.dreambox.core.render.view.IDBCoreView;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * author: chenjing
 * date: 2020/5/6
 */
public class DBEngine {
    private static volatile DBEngine sInstance;

    private Application mApplication;
    private DBNodeParser mDBLoader;

    private DBEngine() {
    }

    public static DBEngine getInstance() {
        if (sInstance == null) {
            synchronized (DBEngine.class) {
                if (sInstance == null) {
                    sInstance = new DBEngine();
                }
            }
        }
        return sInstance;
    }

    public void init(Application application) {
        mApplication = application;
        mDBLoader = new DBNodeParser();
        initInternal();
    }

    public Application getApplication() {
        return mApplication;
    }

    @WorkerThread
    public synchronized JsonObject extWrapper(String extJsonObject) {
        JsonObject extJson = null;
        if (!DBUtils.isEmpty(extJsonObject)) {
            extJson = new Gson().fromJson(extJsonObject, JsonObject.class);
        }
        return extJson;
    }

    @WorkerThread
    public synchronized DBTemplate parser(String accessKey, String templateId, String dblTemplate) {
        return mDBLoader.parser(accessKey, templateId, dblTemplate);
    }

    public IDBCoreView render(String accessKey, DBTemplate template, JsonObject extJsonObject, Context currentContext, Lifecycle lifecycle) {
        if (null != mDBLoader && null != template) {
            DBContext dBContext = new DBContext(mApplication, Wrapper.get(accessKey), currentContext, lifecycle);
            dBContext.setDBTemplate(template);
            // 设置meta数据池
            dBContext.setDBMeta(template.getDBMeta(), extJsonObject);
            // 设置动作集合
            dBContext.setDBAlias(template.getDBAlias());

            // 初始化构建
            template.preProcess(dBContext);
            // 处理树形节点属性
            template.processAttr();
            // 渲染Render子节点
            template.processRender();
            // 处理Action
            template.processAction();
            // 绑定生命周期
            template.bindLifecycle(lifecycle);
            return template.getDreamBoxCoreView();
        }
        return null;
    }

//    public boolean putProperties(String accessKey, DBTemplate template, Map properties) {
//        return true;
//    }
//
//    public boolean putProperty(String accessKey, DBTemplate template, String key, Object value) {
//        return true;
//    }
//
//    public boolean putGlobalProperties(String accessKey, Map properties) {
//        return true;
//    }

    public void putGlobalProperty(String accessKey, String key, String value) {
        DBGlobalPool.get(accessKey).addData(key, value);
    }

    public void putGlobalProperty(String accessKey, String key, int value) {
        DBGlobalPool.get(accessKey).addData(key, value);
    }

    public void putGlobalProperty(String accessKey, String key, boolean value) {
        DBGlobalPool.get(accessKey).addData(key, value);
    }

    public void registerDBNode(String nodeTag, INodeCreator nodeCreator) {
        DBNodeRegistry.registerNode(nodeTag, nodeCreator);
    }

    private void initInternal() {
        registerActionNode();
        registerEventNode();
        registerDataNode();
        registerRenderNode();
    }

    private void registerActionNode() {
        DBNodeRegistry.registerNode(DBClosePage.getNodeTag(), new DBClosePage.NodeCreator());
        DBNodeRegistry.registerNode(DBLog.getNodeTag(), new DBLog.NodeCreator());
        DBNodeRegistry.registerNode(DBTrace.getNodeTag(), new DBTrace.NodeCreator());
        DBNodeRegistry.registerNode(DBTrace.TraceAttr.getNodeTag(), new DBTrace.TraceAttr.NodeCreator());
        DBNodeRegistry.registerNode(DBAlias.getNodeTag(), new DBAlias.NodeCreator());
        DBNodeRegistry.registerNode(DBStorage.getNodeTag(), new DBStorage.NodeCreator());
        DBNodeRegistry.registerNode(DBChangeMeta.getNodeTag(), new DBChangeMeta.NodeCreator());
        DBNodeRegistry.registerNode(DBDialog.getNodeTag(), new DBDialog.NodeCreator());
        DBNodeRegistry.registerNode(DBNav.getNodeTag(), new DBNav.NodeCreator());
        DBNodeRegistry.registerNode(DBNet.getNodeTag(), new DBNet.NodeCreator());
        DBNodeRegistry.registerNode(DBToast.getNodeTag(), new DBToast.NodeCreator());
        DBNodeRegistry.registerNode(DBInvoke.getNodeTag(), new DBInvoke.NodeCreator());
        DBNodeRegistry.registerNode(DBActions.getNodeTag(), new DBActions.NodeCreator());
    }

    private void registerEventNode() {
        DBNodeRegistry.registerNode(DBClick.getNodeTag(), new DBClick.NodeCreator());
        DBNodeRegistry.registerNode(DBVisible.getNodeTag(), new DBVisible.NodeCreator());
        DBNodeRegistry.registerNode(DBInVisible.getNodeTag(), new DBInVisible.NodeCreator());
        DBNodeRegistry.registerNode(DBOnSuccess.getNodeTag(), new DBOnSuccess.NodeCreator());
        DBNodeRegistry.registerNode(DBOnError.getNodeTag(), new DBOnError.NodeCreator());
        DBNodeRegistry.registerNode(DBOnPositive.getNodeTag(), new DBOnPositive.NodeCreator());
        DBNodeRegistry.registerNode(DBOnNegative.getNodeTag(), new DBOnNegative.NodeCreator());

        DBNodeRegistry.registerNode(DBSendEvent.getNodeTag(), new DBSendEvent.NodeCreator());
        DBNodeRegistry.registerNode(DBOnEvent.getNodeTag(), new DBOnEvent.NodeCreator());
        DBNodeRegistry.registerNode(DBBridgeDBToNativeCallback.getNodeTag(), new DBBridgeDBToNativeCallback.NodeCreator());
    }

    private void registerDataNode() {
        DBNodeRegistry.registerNode(DBMeta.getNodeTag(), new DBMeta.NodeCreator());
    }

    private void registerRenderNode() {
        DBNodeRegistry.registerNode(DBTemplate.getNodeTag(), new DBTemplate.NodeCreator());
        DBNodeRegistry.registerNode(DBRender.getNodeTag(), new DBRender.NodeCreator());
        DBNodeRegistry.registerNode(DBView.getNodeTag(), new DBView.NodeCreator());
        DBNodeRegistry.registerNode(DBText.getNodeTag(), new DBText.NodeCreator());
        DBNodeRegistry.registerNode(DBImage.getNodeTag(), new DBImage.NodeCreator());
        DBNodeRegistry.registerNode(DBButton.getNodeTag(), new DBButton.NodeCreator());
        DBNodeRegistry.registerNode(DBLoading.getNodeTag(), new DBLoading.NodeCreator());
        DBNodeRegistry.registerNode(DBProgress.getNodeTag(), new DBProgress.NodeCreator());
        DBNodeRegistry.registerNode(DBList.getNodeTag(), new DBList.NodeCreator());
        DBNodeRegistry.registerNode(DBListVh.getNodeTag(), new DBListVh.NodeCreator());
        DBNodeRegistry.registerNode(DBListHeader.getNodeTag(), new DBListHeader.NodeCreator());
        DBNodeRegistry.registerNode(DBListFooter.getNodeTag(), new DBListFooter.NodeCreator());
        DBNodeRegistry.registerNode(DBFlow.getNodeTag(), new DBFlow.NodeCreator());
        DBNodeRegistry.registerNode(DBChildren.getNodeTag(), new DBChildren.NodeCreator());
    }
}
