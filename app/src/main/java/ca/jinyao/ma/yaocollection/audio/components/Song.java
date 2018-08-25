package ca.jinyao.ma.yaocollection.audio.components;

/**
 * Class Song
 * create by jinyaoMa 0009 2018/8/9 23:42
 */
public class Song {
    private int reference;

    public String songPath;
    public String songId;
    public String songTitle;

    private ArtistList artists;
    private Album album;

    public Song(int reference, String songId, String songTitle) {
        this.reference = reference;
        this.songId = songId;
        this.songTitle = songTitle;
        artists = new ArtistList();
        songPath = "";
    }

    public int getReference() {
        return reference;
    }

    public void addArtist(String artistId, String artistName, String coverPath) {
        Artist artist = new Artist(reference, artistId, artistName);
        if (coverPath != null) {
            artist.coverPath = coverPath;
        }
        artists.add(artist);
    }

    public ArtistList getArtists() {
        return artists;
    }

    public void clearArtists() {
        artists.clear();
    }

    public void setAlbum(String albumId, String albumTitle) {
        album = new Album(reference, albumId, albumTitle);
    }

    public String getCoverPath() {
        if (album != null) {
            return album.coverPath == null ? "" : album.coverPath;
        }
        return "";
    }

    public void setCoverPath(String path) {
        if (album != null) {
            album.coverPath = path;
        }
    }
}