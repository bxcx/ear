package ear.life.ui.music;


import java.util.Comparator;

import static ear.life.ui.music.MusicLoader.*;

public class PinyinComparator implements Comparator<MusicLoader.MusicInfo> {

    @Override
    public int compare(MusicInfo o1, MusicInfo o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }

}
