package ca.jinyao.ma.video.components;

/**
 * Class Catalogue
 * create by jinyaoMa 0001 2018/9/1 21:57
 */
public class Catalogue {
    private String name;
    private String url;
    private Boolean selected = false;

    public Catalogue(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
