package ca.jinyao.ma.video.components;

import java.util.ArrayList;

/**
 * Class CatalogueList
 * create by jinyaoMa 0001 2018/9/1 21:59
 */
public class CatalogueList extends ArrayList<Catalogue> {
    private String tag = "";

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof Catalogue) {
            Catalogue catalogue = (Catalogue) o;
            for (int i = 0; i < size(); i++)
                if (catalogue.getUrl().equals(get(i).getUrl()))
                    return i;
        }
        return -1;
    }
}
