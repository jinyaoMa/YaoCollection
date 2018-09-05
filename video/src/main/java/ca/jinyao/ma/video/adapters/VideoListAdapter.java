package ca.jinyao.ma.video.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ca.jinyao.ma.video.cachers.ImageCacher;
import ca.jinyao.ma.video.components.VideoList;
import ca.jinyao.ma.video.R;

/**
 * Class VideoListAdapter
 * create by jinyaoMa 0004 2018/9/4 16:50
 */
public class VideoListAdapter extends BaseAdapter {
    private Context context;
    private VideoList videos;

    ImageView cover;
    TextView title;
    TextView subtitle;
    TextView ico;

    public VideoListAdapter(Context context) {
        this.context = context;
        videos = new VideoList();
    }

    public void setVideos(@NonNull VideoList videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    public VideoList getVideos() {
        return videos;
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    public Object getItem(int i) {
        return videos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.video_list_item, null);
        }

        cover = view.findViewById(R.id.cover);
        title = view.findViewById(R.id.title);
        subtitle = view.findViewById(R.id.subtitle);
        ico = view.findViewById(R.id.ico);

        ImageCacher.getImage(videos.get(i).getCoverPath(), new ImageCacher.ImageCacheListener() {
            @Override
            public void onCompleted(Bitmap bitmap, String path) {
                cover.setImageBitmap(bitmap);
            }
        });

        title.setText(videos.get(i).getTitle());

        subtitle.setText(videos.get(i).getMessage());

        ico.setText(videos.get(i).getIco());

        if (videos.get(i).getIco().isEmpty()) {
            ico.setVisibility(View.GONE);
        } else {
            ico.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
