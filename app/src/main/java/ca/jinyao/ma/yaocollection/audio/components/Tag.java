package ca.jinyao.ma.yaocollection.audio.components;

/**
 * Class Tag
 * create by jinyaoMa 0009 2018/8/9 22:16
 */
public class Tag {
    private int reference;
    private String name;
    private String id;

    public Tag(int reference, String name, String id) {
        this.reference = reference;
        this.name = name;
        this.id = id;
    }

    public int getReference() {
        return reference;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
