package ca.jinyao.ma.yaocollection.audio.components;

import java.util.ArrayList;

/**
 * Class CategoryList
 * create by jinyaoMa 0009 2018/8/9 22:29
 */
public class CategoryList extends ArrayList<Category> {
    @Override
    public int indexOf(Object o) {
        if (o instanceof Category) {
            Category category = (Category) o;
            for (int i = 0; i < size(); i++)
                if (category.getName().equals(get(i).getName()) && // compare name
                        category.getReference() == get(i).getReference()) // compare reference
                    return i;
        }
        return -1;
    }
}
