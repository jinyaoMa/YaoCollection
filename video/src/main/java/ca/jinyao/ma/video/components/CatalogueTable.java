package ca.jinyao.ma.video.components;

import java.util.ArrayList;

public class CatalogueTable extends ArrayList<CatalogueList> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof CatalogueList) {
            CatalogueList catalogues = (CatalogueList) o;
            for (int i = 0; i < size(); i++)
                if (catalogues.getTag().equals(get(i).getTag()))
                    return i;
        }
        return -1;
    }
}
