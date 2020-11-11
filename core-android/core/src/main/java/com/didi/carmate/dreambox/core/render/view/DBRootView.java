package com.didi.carmate.dreambox.core.render.view;

import android.util.AttributeSet;

import androidx.constraintlayout.solver.widgets.ConstraintWidget;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.base.IDBNode;
import com.didi.carmate.dreambox.core.render.DBBaseView;
import com.didi.carmate.dreambox.core.render.IDBRender;
import com.didi.carmate.dreambox.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * author: chenjing
 * date: 2020/5/22
 */
public class DBRootView extends ConstraintLayout {
    private DBContext mDBContext;

    public DBRootView(DBContext dbContext) {
        super(dbContext.getContext(), null);
        mDBContext = dbContext;
    }

    public DBRootView(DBContext dbContext, AttributeSet attrs) {
        super(dbContext.getContext(), attrs);
        mDBContext = dbContext;
    }

    public DBRootView(DBContext dbContext, AttributeSet attrs, int defStyleAttr) {
        super(dbContext.getContext(), attrs, defStyleAttr);
        mDBContext = dbContext;
    }

    public void onRenderFinish(IDBRender dbRender) {
        // chain 处理
        List<IDBNode> childNodes = dbRender.getChildNodes();
        if (null == childNodes || childNodes.size() == 0) {
            Wrapper.get(mDBContext.getAccessKey()).log().e("childNotes is empty.");
            return;
        }

        List<DBBaseView> chainHeader = getChainHeaders(childNodes);
        if (chainHeader.size() == 0) {
            Wrapper.get(mDBContext.getAccessKey()).log().d("chain header is empty.");
            return;
        }

        List<DBChain> listChain = new ArrayList<>();
        for (DBBaseView header : chainHeader) {
            listChain.add(getDBChain(header));
        }
        if (listChain.size() == 0) {
            Wrapper.get(mDBContext.getAccessKey()).log().d("chain is empty.");
            return;
        }

        for (DBChain chain : listChain) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            if (chain.chainOrientation == DBChain.CHAIN_ORIENTATION_H) {
                constraintSet.setHorizontalChainStyle(chain.id, chain.chainType);
            } else if (chain.chainOrientation == DBChain.CHAIN_ORIENTATION_V) {
                constraintSet.setVerticalChainStyle(chain.id, chain.chainType);
            }
            constraintSet.applyTo(this);
        }
    }

    private List<DBBaseView> getChainHeaders(List<IDBNode> childNodes) {
        List<DBBaseView> chainHeader = new ArrayList<>();
        if (null != childNodes) {
            for (IDBNode dbNote : childNodes) {
                if (dbNote instanceof DBBaseView) {
                    DBBaseView dbBaseView = (DBBaseView) dbNote;
                    String chainStyle = dbBaseView.getChainStyle();
                    if (DBConstants.STYLE_CHAIN_SPREAD.equals(chainStyle) ||
                            DBConstants.STYLE_CHAIN_SPREAD_INSIDE.equals(chainStyle) ||
                            DBConstants.STYLE_CHAIN_PACKED.equals(chainStyle)) {
                        chainHeader.add(dbBaseView);
                    }
                }
            }
        }
        return chainHeader;
    }

    private DBChain getDBChain(DBBaseView header) {
        DBChain dbChain = new DBChain();
        dbChain.id = header.getId();
        if (header.getLeftToLeft() != DBConstants.DEFAULT_ID_VIEW) {
            dbChain.chainOrientation = DBChain.CHAIN_ORIENTATION_H;
        } else if (header.getTopToTop() != DBConstants.DEFAULT_ID_VIEW) {
            dbChain.chainOrientation = DBChain.CHAIN_ORIENTATION_V;
        }
        dbChain.chainType = convertChainType(header.getChainStyle());
        return dbChain;
    }

    private int convertChainType(String chainStyle) {
        int chainType = ConstraintWidget.UNKNOWN;
        if (DBConstants.STYLE_CHAIN_SPREAD.equals(chainStyle)) {
            chainType = ConstraintWidget.CHAIN_SPREAD;
        } else if (DBConstants.STYLE_CHAIN_SPREAD_INSIDE.equals(chainStyle)) {
            chainType = ConstraintWidget.CHAIN_SPREAD_INSIDE;
        } else if (DBConstants.STYLE_CHAIN_PACKED.equals(chainStyle)) {
            chainType = ConstraintWidget.CHAIN_PACKED;
        }
        return chainType;
    }

    private static final class DBChain {
        static final int CHAIN_ORIENTATION_H = 1;
        static final int CHAIN_ORIENTATION_V = 2;

        int chainOrientation = 0;
        int id;
        int chainType = 0;
    }
}
