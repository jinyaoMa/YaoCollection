package ca.jinyao.ma.audio.components;

import java.util.ArrayList;

/**
 * Class SongList
 * create by jinyaoMa 0009 2018/8/9 23:09
 */
public class SongList extends ArrayList<Song> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Song) {
            Song song = (Song) o;
            for (int i = 0; i < size(); i++)
                if (song.songId.equals(get(i).songId) &&
                        song.getReference() == get(i).getReference())
                    return i;
        }
        return -1;
    }
}
