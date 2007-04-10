package org.pyxx.dl.conv;

import java.util.Hashtable;

/**
 * Date: 23.07.2004
 * Time: 23:29:17
 */
public class ParamsHelper {

    public static Hashtable parseSimplePairs(String[] params) {
        Hashtable pairs = new Hashtable();
        int pairsnum = (int) Math.floor(((double) params.length) / 2);
        for(int i = 0; i < pairsnum; i++) {
            pairs.put(params[i*2], params[i*2 + 1]);
        }
        return pairs;
    }

}
