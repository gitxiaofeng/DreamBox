package com.didi.carmate.catalog.page;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.didi.carmate.catalog.support.SampleListAdapter;
import com.didi.dreambox.catalog.R;

import java.util.ArrayList;
import java.util.List;

public class ExpandListActivity extends AppCompatActivity {
    private RecyclerView list;
    private SampleListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand_list);
        list = findViewById(R.id.catalog_expand_list);
        initView();
    }

    private void initView() {
        adapter = new SampleListAdapter();
        adapter.setDataList(getData());
        adapter.setActivity(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
    }

    public List<SampleListAdapter.SampleListItem> getData() {
        ArrayList<SampleListAdapter.SampleListItem> arrayList = new ArrayList<>();
        String[] listname = getResources().getStringArray(R.array.item_expand_name);
        String[] dbname = getResources().getStringArray(R.array.item_expand_dbname);
        if (listname.length == dbname.length) {
            for (int i = 0; i < listname.length; i++) {
                SampleListAdapter.SampleListItem sampleListItem = new SampleListAdapter.SampleListItem();
                sampleListItem.name = listname[i];
                sampleListItem.dbName = dbname[i];
                sampleListItem.id = 99;
                arrayList.add(sampleListItem);
            }
        } else {
            throw new RuntimeException("item_expand_name跟item_expand_dbname数量不一致！");
        }
        return arrayList;
    }
}
