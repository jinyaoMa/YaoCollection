package ca.jinyao.ma.audio.components;

import java.util.ArrayList;

/**
 * Class Category
 * create by jinyaoMa 0009 2018/8/9 22:21
 */
public class Category {
    private int reference;
    private String name;
    private ArrayList<Tag> tags;

    public Category(int reference, String name) {
        this.reference = reference;
        this.name = name;
        tags = new ArrayList<>();
    }

    public int getReference() {
        return reference;
    }

    public String getName() {
        return name;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }
}