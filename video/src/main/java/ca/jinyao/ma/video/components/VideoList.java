package ca.jinyao.ma.video.components;

import java.util.ArrayList;

/**
 * Class VideoList
 * create by jinyaoMa 0031 2018/8/31 22:47
 */
public class VideoList extends ArrayList<Video> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Video) {
            Video video = (Video) o;
            for (int i = 0; i < size(); i++)
                if (video.getUrl().equals(get(i).getUrl()))
                    return i;
        }
        return -1;
    }
}
