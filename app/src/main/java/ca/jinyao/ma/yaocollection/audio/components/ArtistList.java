package ca.jinyao.ma.yaocollection.audio.components;

import java.util.ArrayList;

/**
 * Class ArtistList
 * create by jinyaoMa 0009 2018/8/9 23:43
 */
public class ArtistList extends ArrayList<Artist> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Artist) {
            Artist artist = (Artist) o;
            for (int i = 0; i < size(); i++)
                if (artist.artistId.equals(get(i).artistId) &&
                        artist.getReference() == get(i).getReference())
                    return i;
        }
        return -1;
    }

    public String getNameString() {
        String result = "";
        for (int i = 0; i < size(); i++) {
            if (!result.isEmpty()) {
                result += " / ";
            }
            result += get(i).artistName;
        }
        return result;
    }
}
