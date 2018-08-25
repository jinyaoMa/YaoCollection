package ca.jinyao.ma.yaocollection.audio.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class Lyric
 * create by jinyaoMa 0011 2018/8/11 23:44
 */
public class Lyric {
    private TreeMap<Long, String> lyric;
    private ArrayList<Long> times;
    private ArrayList<Line> lines;
    private int length;
    private int currentIndex;
    private Boolean noError;

    public Lyric(String raw) {
        lyric = new TreeMap<>(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                if ((o2 - o1) > 0) {
                    return 1;
                } else if ((o2 - o1) < 0) {
                    return -1;
                }
                return 0;
            }
        });

        String[] temp = raw.split("\\n");
        Pattern pattern = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2})\\.(\\d{1,3})\\](.*)"); // [00:00.000]...
        for (int i = 0; i < temp.length; i++) {
            Matcher matcher = pattern.matcher(temp[i]);
            if (matcher.find()) {
                long milliseconds = Long.parseLong(matcher.group(1));
                milliseconds = Long.parseLong(matcher.group(2)) + milliseconds * 60;
                milliseconds = Long.parseLong(matcher.group(3)) + milliseconds * 1000;
                lyric.put(milliseconds, matcher.group(4));
            }
        }

        times = new ArrayList<>();
        lines = new ArrayList<>();
        if (lyric.size() > 0) {
            Iterator<Long> iterator = lyric.descendingKeySet().iterator();
            while (iterator.hasNext()) {
                long time = iterator.next();
                times.add(time);
                lines.add(new Line(lyric.get(time)));
            }
            for (int i = 0; i < lines.size() - 1; i++) { // the last line does not have "nextWords" attribute
                lines.get(i).setNextWords(lines.get(i + 1).getWords());
            }
            currentIndex = 0;
            if (lines.size() == times.size()) {
                length = lines.size();
                noError = true;
            } else {
                noError = false;
            }
        } else {
            noError = false;
        }
    }

    public int getIndexAtTimeline(long timeline) {
        if (noError) {
            for (int i = (length - 1); i >= 0; i--) {
                if (timeline >= times.get(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public Line getLineAtTimeline(long timeline) {
        if (noError) {
            for (int i = (length - 1); i >= 0; i--) {
                if (timeline >= times.get(i)) {
                    currentIndex = i;
                    return lines.get(i);
                }
            }
        }
        return null;
    }

    public Line getLineAtIndex(int index) {
        if (noError) {
            if (index < length && index >= 0) {
                currentIndex = index;
                return lines.get(index);
            }
        }
        return null;
    }

    public Line getNextLine() {
        if (noError) {
            if ((currentIndex + 1) < length) {
                currentIndex += 1;
                return lines.get(currentIndex);
            }
        }
        return null;
    }

    public long getCurrentTime() {
        if (noError) {
            return times.get(currentIndex);
        }
        return 0;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public class Line {
        private String words;
        private String nextWords;

        public Line(String words) {
            this.words = words;
            this.nextWords = "";
        }

        public void setNextWords(String nextWords) {
            this.nextWords = nextWords;
        }

        public String getNextWords() {
            return nextWords;
        }

        public String getWords() {
            return words;
        }
    }
}
