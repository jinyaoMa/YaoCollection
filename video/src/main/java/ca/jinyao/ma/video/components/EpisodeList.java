package ca.jinyao.ma.video.components;

import java.util.ArrayList;

/**
 * Class EpisodeList
 * create by jinyaoMa 0031 2018/8/31 21:44
 */
public class EpisodeList extends ArrayList<Episode> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Episode) {
            Episode episode = (Episode) o;
            for (int i = 0; i < size(); i++)
                if (episode.getDownloadUrl().equals(get(i).getDownloadUrl()))
                    return i;
        }
        return -1;
    }
}
