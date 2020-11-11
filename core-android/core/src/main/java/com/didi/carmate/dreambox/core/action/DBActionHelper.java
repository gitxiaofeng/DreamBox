package com.didi.carmate.dreambox.core.action;

import com.didi.carmate.dreambox.core.base.DBActionPool;
import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.bridge.DBBridgeDBToNativeCallback;
import com.didi.carmate.dreambox.core.bridge.DBOnEvent;
import com.didi.carmate.dreambox.core.callback.DBOnError;
import com.didi.carmate.dreambox.core.callback.DBOnNegative;
import com.didi.carmate.dreambox.core.callback.DBOnPositive;
import com.didi.carmate.dreambox.core.callback.DBOnSuccess;
import com.didi.carmate.dreambox.core.callback.DBClick;
import com.didi.carmate.dreambox.core.callback.DBListOnMore;
import com.didi.carmate.dreambox.core.callback.DBListOnPull;
import com.didi.carmate.dreambox.core.callback.DBInVisible;
import com.didi.carmate.dreambox.core.callback.DBVisible;
import com.didi.carmate.dreambox.core.callback.IDBCallback;
import com.didi.carmate.dreambox.core.utils.DBUtils;
import com.didi.carmate.dreambox.wrapper.Wrapper;
import com.didi.carmate.dreambox.wrapper.inner.WrapperMonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: chenjing
 * date: 2020/5/18
 */
public class DBActionHelper {
    public static void addNodeActionToPool(IDBNode dbNode, DBActionPool dBActionPool, DBContext dBContext) {
        List<IDBNode> childNodes = dbNode.getChildNodes();
        if (null != childNodes) {
            for (IDBNode childNode : childNodes) {
                if (!(childNode instanceof IDBCallback)) {
                    continue;
                }

                if (childNode instanceof DBClick) {
                    List<IDBNode> actionChildNodes = ((DBClick) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onClick] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putClickAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBVisible) {
                    List<IDBNode> actionChildNodes = ((DBVisible) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onVisible] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putVisibleAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBInVisible) {
                    List<IDBNode> actionChildNodes = ((DBInVisible) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onInvisible] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putInvisibleAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBOnSuccess) {
                    List<IDBNode> actionChildNodes = ((DBOnSuccess) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onSuccess] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putCallbackNetSuccessAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBOnError) {
                    List<IDBNode> actionChildNodes = ((DBOnError) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onError] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putCallbackNetErrorAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBOnPositive) {
                    List<IDBNode> actionChildNodes = ((DBOnPositive) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onPositive] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putCallbackDialogPositiveAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBOnNegative) {
                    List<IDBNode> actionChildNodes = ((DBOnNegative) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onNegative] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putCallbackDialogNegativeAction((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBListOnPull) {
                    List<IDBNode> actionChildNodes = ((DBListOnPull) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onPull] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putListOnPull((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBListOnMore) {
                    List<IDBNode> actionChildNodes = ((DBListOnMore) childNode).getActions();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onMore] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putListOnMore((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBOnEvent) {
                    List<IDBNode> actionChildNodes = childNode.getChildNodes();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][onEvent] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putBridgeOnEvent((IDBAction) actionChildNode);
                        }
                    }
                } else if (childNode instanceof DBBridgeDBToNativeCallback) {
                    List<IDBNode> actionChildNodes = childNode.getChildNodes();
                    if (null == actionChildNodes) {
                        String simpleName = dbNode.getClass().getSimpleName();
                        Wrapper.get(dBContext.getAccessKey()).log().e("node [" + simpleName + "][callback] child is empty!");
                        return;
                    }
                    for (IDBNode actionChildNode : actionChildNodes) {
                        if (actionChildNode instanceof DBInvoke) {
                            invokeActionProcess(dBActionPool, dBContext, (DBInvoke) actionChildNode);
                        } else if (actionChildNode instanceof IDBAction) {
                            dBActionPool.putBridgeCallbackDBToN((IDBAction) actionChildNode);
                        }
                    }
                }
            }
        }
    }

    private static void invokeActionProcess(DBActionPool dBActionPool, DBContext dBContext, DBInvoke dbInvoke) {
        String aliasId = dbInvoke.getAlias();
        if (DBUtils.isEmpty(aliasId)) {
            Wrapper.get(dBContext.getAccessKey()).log().e("invoke note aliasId is empty.");
            return;
        }

        DBAlias dbAlias = dBContext.getDBAlias(aliasId);
        if (null == dbAlias) {
            Wrapper.get(dBContext.getAccessKey()).log().e("aliasId " + aliasId + " not found.");
            Map<String, String> params = new HashMap<>();
            params.put("alias_id", aliasId);
            Wrapper.get(dBContext.getAccessKey()).monitor()
                    .report(dBContext.getTemplateId(), DBConstants.TRACE_ACTION_ALIAS_NOT_FOUND, WrapperMonitor.TRACE_NUM_EVERY)
                    .addAll(params).report();
            return;
        }

        // 添加进事件集合
        IDBNode parentNode = dbInvoke.getActionCallback();
        List<IDBNode> childNodes = dbAlias.getActions();
        if (null != childNodes && null != parentNode) {
            for (IDBNode childNode : childNodes) {
                IDBAction actionNode = (IDBAction) childNode;
                actionNode.setInvokeJsonObject(dbInvoke.getSrc());
                actionNode.setAliasJsonObject(dbAlias.getSrc());

                if (parentNode instanceof DBClick) {
                    dBActionPool.putClickAction(actionNode);
                } else if (parentNode instanceof DBVisible) {
                    dBActionPool.putVisibleAction(actionNode);
                } else if (parentNode instanceof DBInVisible) {
                    dBActionPool.putInvisibleAction(actionNode);
                } else if (parentNode instanceof DBOnSuccess) {
                    dBActionPool.putCallbackNetSuccessAction(actionNode);
                } else if (parentNode instanceof DBOnError) {
                    dBActionPool.putCallbackNetErrorAction(actionNode);
                } else if (parentNode instanceof DBOnPositive) {
                    dBActionPool.putCallbackDialogPositiveAction(actionNode);
                } else if (parentNode instanceof DBOnNegative) {
                    dBActionPool.putCallbackDialogNegativeAction(actionNode);
                } else if (parentNode instanceof DBListOnPull) {
                    dBActionPool.putListOnPull(actionNode);
                } else if (parentNode instanceof DBListOnMore) {
                    dBActionPool.putListOnMore(actionNode);
                } else if (parentNode instanceof DBOnEvent) {
                    dBActionPool.putBridgeOnEvent(actionNode);
                } else if (parentNode instanceof DBBridgeDBToNativeCallback) {
                    dBActionPool.putBridgeCallbackDBToN(actionNode);
                }
            }
        }
    }
}
