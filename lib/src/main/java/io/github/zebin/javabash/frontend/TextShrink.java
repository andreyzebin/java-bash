package io.github.zebin.javabash.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>/Users/andreyzabebenin/tmp/3456/gitsql-test_112d></p>
 * <p>/Users/a../t../3../gitsql-test_112d></p>
 * <p>/U..///3../gitsql-test_112d></p>
 * <p>/////gitsql-te..></p>
 *
 *
 *
 * <p>shrinking</p>
 * <p>/Users/</p>
 * <p>/Us../</p>
 * <p>/U../</p>
 * <p>//</p>
 *
 *
 * <p>rigidity - k</p>
 * <p>/10x/x/100x/</p>
 *
 * <p>/f/f/f/</p>
 *
 * <p>f=K x X</p>
 *
 * <p>while (size > wantedSize) {</p>
 * <p>	find iSEG where f = min or k = min</p>
 * <p>	iSEG.size -=1</p>
 * <p>	recalc all f</p>
 * <p>}</p>
 */
public class TextShrink {


    /**
     * abcd 3 -> a.. a.. 2 -> ''
     */
    public static String getShrinkWord(String origin, int limit) {
        int origLength = origin.length();

        if (limit > 2 && limit < origLength) {
            return origin.substring(0, limit - 2) + "..";
        } else if (limit <= 2) {
            return "";
        }

        return origin;
    }


    public static String getShrinkDir(String dir, int limit) {
        List<Entry<String, String>> list = new ArrayList<>(
                Stream.of(dir.split("/")).map(k -> Map.entry(k, k)).toList());
        Map<Integer, Integer> forces = new HashMap<>();
        Map<Integer, Integer> rigids = new HashMap<>();
        int lenBefore = list.stream().map(Entry::getValue).collect(Collectors.joining("/")).length();

        while (list.stream().map(Entry::getValue).collect(Collectors.joining("/")).length() > limit) {

            for (int i = 0; i < list.size(); i++) {
                int k = 1;
                if (i == 0) {
                    k = 10;
                }
                if (i == 1 && list.get(0).getKey().isEmpty()) {
                    k = 10;
                }
                if (i == list.size() - 1) {
                    k = 100;
                }

                int i1 = Math.max(0, list.get(i).getValue().length() - 1);

                int dif = list.get(i).getKey().length() - i1;
                if (dif == list.get(i).getKey().length()) {
                    k = 1000;
                    dif = 1000;
                }
                int f = dif * k;
                forces.put(i, f);
                rigids.put(i, k);
            }

            int minF = forces.values().stream().min(Integer::compareTo).get();
            Integer key = forces.entrySet().stream().filter(f -> minF == f.getValue())
                    .min((o1, o2) -> rigids.get(o1.getKey()).compareTo(o2.getKey())).get().getKey();

            int newValueLength = list.get(key).getValue().length() - 1;
            list.set(key, Map.entry(list.get(key).getKey(), getShrinkWord(list.get(key).getKey(), newValueLength)));

            if (list.stream().map(Entry::getValue).collect(Collectors.joining("/")).length() == lenBefore) {
                break;
            }
            lenBefore = list.stream().map(Entry::getValue).collect(Collectors.joining("/")).length();
        }

        return list.stream().map(Entry::getValue).collect(Collectors.joining("/"));
    }

}
