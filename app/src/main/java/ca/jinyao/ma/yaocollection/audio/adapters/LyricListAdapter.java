package ca.jinyao.ma.yaocollection.audio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.components.Lyric;

/**
 * Class LyricListAdapter
 * create by jinyaoMa 0024 2018/8/24 17:44
 */
public class LyricListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Lyric.Line> lines;
    private int highlightIndex;

    @BindView(R.id.line)
    TextView line;

    public LyricListAdapter(Context context) {
        this.context = context;
        lines = new ArrayList<>();
        highlightIndex = -1;
    }

    public void setLines(ArrayList<Lyric.Line> lines) {
        this.lines = lines;
    }

    @Override
    public int getCount() {
        return lines.size();
    }

    @Override
    public Object getItem(int i) {
        return lines.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.lyric_list_item, null);
        }

        ButterKnife.bind(this, view);

        line.setText(lines.get(i).getWords());

        if (highlightIndex == i) {
            line.setTextColor(context.getColor(R.color.colorPrimary));
        } else {
            line.setTextColor(Color.BLACK);
        }

        return view;
    }

    public void setHighlight(int index) {
        highlightIndex = index;
        notifyDataSetChanged();
    }
}
