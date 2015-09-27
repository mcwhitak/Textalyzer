package com.whitaker.textalyzer;

import android.app.Activity;
import android.os.Bundle;
import com.whitaker_iacob.textalyzer.R;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

public class OpenSourceActivity extends Activity {

    private ListView listView;
    int license[] = {R.string.license_williamchart};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opensource);
        listView = (ListView)findViewById(R.id.listview);
        listView.setAdapter(new OpenSourceAdapter());
    }

    private class OpenSourceAdapter extends BaseAdapter {
        public int getCount() {
            return license.length;
        }

        public long getItemId(int pos) {
            return pos;
        }

        public Object getItem(int pos) {
            return license[pos];
        }

        public View getView(int pos, View convert, ViewGroup parent) {
            if(convert == null) {
                LayoutInflater li = getLayoutInflater();
                convert = li.inflate(R.layout.opensource_item, null);
            }

            TextView text = (TextView)convert.findViewById(R.id.opensource_text);
            if(pos < license.length) {
                text.setTextSize(18);
                text.setText(getResources().getString(license[pos]));
                text.setClickable(false);
            }
            return text;
        }
    }
}