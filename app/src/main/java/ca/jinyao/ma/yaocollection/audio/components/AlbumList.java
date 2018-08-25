package ca.jinyao.ma.yaocollection.audio.components;

import java.util.ArrayList;

/**
 * Class AlbumList
 * create by jinyaoMa 0009 2018/8/9 23:45
 */
public class AlbumList extends ArrayList<Album> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Album) {
            Album album = (Album) o;
            for (int i = 0; i < size(); i++)
                if (album.albumId.equals(get(i).albumId) &&
                        album.getReference() == get(i).getReference())
                    return i;
        }
        return -1;
    }
}
