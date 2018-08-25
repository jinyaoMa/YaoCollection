package ca.jinyao.ma.yaocollection.audio.components;

import java.util.ArrayList;

/**
 * Class PlaylistList
 * create by jinyaoMa 0009 2018/8/9 23:06
 */
public class PlaylistList extends ArrayList<Playlist> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Playlist) {
            Playlist playlist = (Playlist) o;
            for (int i = 0; i < size(); i++)
                if (playlist.id.equals(get(i).id) &&
                        playlist.getReference() == get(i).getReference())
                    return i;
        }
        return -1;
    }
}
