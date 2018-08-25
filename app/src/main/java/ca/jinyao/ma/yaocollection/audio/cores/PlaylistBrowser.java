package ca.jinyao.ma.yaocollection.audio.cores;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.io.IOException;

import ca.jinyao.ma.yaocollection.audio.components.Category;
import ca.jinyao.ma.yaocollection.audio.components.CategoryList;
import ca.jinyao.ma.yaocollection.audio.components.Playlist;
import ca.jinyao.ma.yaocollection.audio.components.PlaylistList;
import ca.jinyao.ma.yaocollection.audio.components.Tag;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.NONE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.PAGE_ITEM_LIMIT;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAG;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.connectionForPlaylistBrowser;

/**
 * Class PlaylistBrowser
 * create by jinyaoMa 0009 2018/8/9 23:08
 */
public class PlaylistBrowser {
    private PlaylistBrowseListener playlistBrowseListener;

    public interface PlaylistBrowseListener {
        void onTagsGet(CategoryList categories, Tag defaultTag, Boolean hasDefaultTag);

        void onPlaylistsGet(PlaylistList playlists, int nextPage, Boolean hasNextPage);
    }

    public PlaylistBrowser(PlaylistBrowseListener playlistBrowseListener) {
        this.playlistBrowseListener = playlistBrowseListener;
    }

    public void getTagsFor(int ref) {
        GetTagsTask getTagsTask = new GetTagsTask();
        getTagsTask.execute(ref);
    }

    public void getPlaylists(int ref, String tagId) {
        GetPlaylistsTask getPlaylistsTask = new GetPlaylistsTask();
        getPlaylistsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ref, tagId);
    }

    public void getPlaylists(int ref, String tagId, int page) {
        GetPlaylistsTask getPlaylistsTask = new GetPlaylistsTask();
        getPlaylistsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ref, tagId, page);
    }

    private class GetPlaylistsTask extends AsyncTask<Object, PlaylistList, PlaylistList> {
        private int pageNumber;
        private Boolean hasNextPage;

        @Override
        protected PlaylistList doInBackground(Object... objects) {
            PlaylistList playlists = new PlaylistList();

            if (objects.length >= 2) {
                int ref = (int) objects[0];
                String tabId = (String) objects[1];
                pageNumber = 0;
                if (objects.length == 3) {
                    pageNumber = (int) objects[2];
                }
                Connection connection = connectionForPlaylistBrowser(ref, tabId, pageNumber);

                try {
                    JSONObject jsonObject;
                    JSONArray jsonArray;
                    int length;
                    switch (ref) {
                        case REF_QQ:
                            jsonObject = new JSONObject(connection.execute().body())
                                    .optJSONObject("data");
                            jsonArray = jsonObject.optJSONArray("list");
                            length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject temp = jsonArray.optJSONObject(i);

                                Playlist playlist = new Playlist(ref, temp.optString("dissid"), temp.optString("dissname"), null);
                                playlist.setCover(temp.optString("imgurl"));

                                playlists.add(playlist);
                            }

                            if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("sum")) {
                                pageNumber += 1;
                                hasNextPage = true;
                            } else {
                                pageNumber = NONE;
                                hasNextPage = false;
                            }
                            break;
                        case REF_163:
                            jsonObject = new JSONObject(connection.execute().body());
                            jsonArray = jsonObject.optJSONArray("playlists");
                            length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject temp = jsonArray.optJSONObject(i);

                                Playlist playlist = new Playlist(ref, temp.optString("id"), temp.optString("name"), null);
                                playlist.setCover(temp.optString("coverImgUrl"));

                                playlists.add(playlist);
                            }

                            if (jsonObject.optBoolean("more")) {
                                pageNumber += 1;
                                hasNextPage = true;
                            } else {
                                pageNumber = NONE;
                                hasNextPage = false;
                            }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return playlists;
        }

        @Override
        protected void onPostExecute(PlaylistList playlists) {
            if (playlists != null && playlists.size() > 0) {
                playlistBrowseListener.onPlaylistsGet(playlists, pageNumber, hasNextPage);
            }
        }
    }

    private class GetTagsTask extends AsyncTask<Integer, CategoryList, CategoryList> {
        private Tag defaulltTag;
        private Boolean hasDefaultTag;

        @Override
        protected CategoryList doInBackground(Integer... integers) {
            CategoryList categories = new CategoryList();
            hasDefaultTag = false;

            if (integers.length == 1) {
                int ref = integers[0];
                Connection connection = connectionForPlaylistBrowser(ref, null, NONE);

                try {
                    JSONObject jsonObject;
                    JSONArray jsonArray;
                    int length;
                    switch (ref) {
                        case REF_QQ:
                            jsonObject = new JSONObject(connection.execute().body())
                                    .optJSONObject("data");
                            jsonArray = jsonObject.optJSONArray("categories");
                            length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject temp = jsonArray.optJSONObject(i);
                                Category category = new Category(ref, temp.optString("categoryGroupName"));

                                JSONArray tmp = temp.optJSONArray("items");
                                for (int j = 0; j < tmp.length(); j++) {
                                    String tagName = tmp.optJSONObject(j).optString("categoryName");
                                    String tagId = tmp.optJSONObject(j).optString("categoryId");
                                    Tag tag = new Tag(ref, tagName, tagId);
                                    category.addTag(tag);

                                    if (tmp.optJSONObject(j).optInt("usable") == 0) {
                                        defaulltTag = tag;
                                        hasDefaultTag = true;
                                    }
                                }

                                categories.add(category);
                            }
                            break;
                        case REF_163:
                            jsonObject = new JSONObject(connection.execute().body());

                            JSONObject temp = jsonObject.optJSONObject("categories");
                            length = temp.length();
                            for (int i = 0; i < temp.length(); i++) {
                                if (temp.has(String.valueOf(i))) {
                                    Category category = new Category(ref, temp.optString(String.valueOf(i)));
                                    categories.add(i, category);
                                }
                            }

                            jsonArray = jsonObject.optJSONArray("sub");
                            length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                temp = jsonArray.optJSONObject(i);
                                int catNum = temp.optInt("category");
                                String tagName = temp.optString("name");
                                String tagId = temp.optString("name");
                                Tag tag = new Tag(ref, tagName, tagId);
                                categories.get(catNum).addTag(tag);
                            }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return categories;
        }

        @Override
        protected void onPostExecute(CategoryList categories) {
            if (categories != null && categories.size() > 0) {
                playlistBrowseListener.onTagsGet(categories, defaulltTag, hasDefaultTag);
            }
        }
    }
}