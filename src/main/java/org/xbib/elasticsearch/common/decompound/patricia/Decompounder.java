package org.xbib.elasticsearch.common.decompound.patricia;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Decompounder.
 */
public class Decompounder {

    private CompactPatriciaTrie kompvvTree;
    private CompactPatriciaTrie kompvhTree;
    private CompactPatriciaTrie grfTree;

    public Decompounder(InputStream kompvv, InputStream kompvh, InputStream gfred, double threshold)
            throws IOException {
        kompvvTree = new CompactPatriciaTrie();
        kompvvTree.load(kompvv);
        kompvvTree.setIgnoreCase(true);
        kompvvTree.setThreshold(threshold);
        kompvhTree = new CompactPatriciaTrie();
        kompvhTree.load(kompvh);
        kompvhTree.setIgnoreCase(true);
        kompvhTree.setThreshold(threshold);
        grfTree = new CompactPatriciaTrie();
        grfTree.load(gfred);
        grfTree.setIgnoreCase(true);
        grfTree.setThreshold(threshold); // previous value = 0.46
    }

    public Decompounder(CompactPatriciaTrie kompvv, CompactPatriciaTrie kompvh, CompactPatriciaTrie gfred, double threshold) {
        kompvvTree = kompvv;
        kompvhTree = kompvh;
        grfTree = gfred;
        grfTree.setThreshold(threshold); // previous value = 0.46
    }

    private String reverse(String torev) {
        StringBuilder ret = new StringBuilder();
        for (int i = torev.length(); i > 0; i--) {
            ret.append(torev.substring(i - 1, i));
        }
        return ret.toString();
    }

    public List<String> decompound(String string) {
        String word = string;
        word = reduceToBaseForm(word);
        List<String> list = new ArrayList<>();
        String classvv = kompvvTree.classify(word + "<");
        String classvh = kompvhTree.classify(reverse(word) + "<");
        StringBuilder numStrvv = new StringBuilder();
        StringBuilder numStrvh = new StringBuilder();
        StringBuilder suffixvv = new StringBuilder();
        StringBuilder suffixvh = new StringBuilder();
        String vvpart1 = "";
        String vhpart1 = "";
        String vvpart2 = "";
        String vhpart2 = "";
        int numvv = 0;
        int numvh = 0;
        boolean vhOk = true;
        boolean vvOk = true;
        if ("undecided".equals(classvv)) {
            vvOk = false;
        }
        if ("undecided".equals(classvh)) {
            vhOk = false;
        }
        if (vvOk) {
            for (int i = 0; i < classvv.length(); i++) {
                char c = classvv.charAt(i);
                if ((c <= '9') && (c >= '0')) {
                    numStrvv.append(c);
                } else {
                    suffixvv.append(c);
                }
            }
        }
        if (vhOk) {
            for (int i = 0; i < classvh.length(); i++) {
                char c = classvh.charAt(i);
                if ((c <= '9') && (c >= '0')) {
                    numStrvh.append(c);
                } else {
                    suffixvh.append(c);
                }
            }
        }

        if (vvOk) {
            numvv = Integer.parseInt(numStrvv.toString());
        }
        if (vhOk) {
            numvh = Integer.parseInt(numStrvh.toString());
        }

        if (vvOk && numvv >= word.length()) {
            vvOk = false;
        }

        if (vhOk && numvh >= word.length()) {
            vhOk = false;
        }

        if (vvOk) {
            for (int i = 0; i < suffixvv.length(); i++) {
                if (word.length() > (numvv + i)) {
                    if (suffixvv.charAt(i) != word.charAt(numvv + i)) {
                        vvOk = false;
                    }
                } else {
                    vvOk = false;
                }
            }
        }
        if (vhOk) {
            for (int i = 0; i < suffixvh.length(); i++) {
                if (suffixvh.charAt(i) != word.charAt(numvh + 1 + i)) {
                    vvOk = false;
                }
            }
        }
        if (vvOk) {
            vvpart1 = word.substring(0, numvv);
            vvpart2 = word.substring(numvv + suffixvv.length(), word.length());
            if (vvpart2.length() <= 3) {
                vvOk = false;
            }
        }
        if (vhOk) {
            vhpart1 = word.substring(0, word.length() - numvh);
            vhpart2 = word.substring(word.length() - (numvh + suffixvh.length()), word.length());
            if (vhpart1.length() <= 3) {
                vhOk = false;
            }
        }
        if (vvOk && vhOk) {
            if ((vvpart1.equals(vhpart1)) || ((vhpart1.length() - vvpart1.length()) < 3)) {
                list.add(vvpart1);
                if (vhpart2.length() < vvpart2.length()) {
                    list.add(vhpart2);
                } else if (vhpart2.length() > vvpart2.length()) {
                    list.add(vvpart2);
                }
            } else {
                list.add(vvpart1);
                list.add(word.substring(vvpart1.length() + suffixvv.length(), word.length() - numvh));
                list.add(vhpart2);
            }
            if (vvpart2.equals(vhpart2)) {
                list.add(vvpart2);
            }

        } else if (vvOk && !vhOk) {
            list.add(vvpart1);
            list.add(vvpart2);
        } else if (vhOk && !vvOk) {
            list.add(vhpart1);
            list.add(vhpart2);
        } else {
            list.add(word);
        }
        List<String> retvec2 = new ArrayList<>();
        List<String> l;
        if (list.size() > 1) {
            for (String s : list) {
                l = decompound(s);
                retvec2.addAll(l);
            }
        } else {
            retvec2 = list;
        }
        return retvec2;
    }

    public String reduceToBaseForm(String word) {
        String result = word;
        String baseForm = grfTree.classify(reverse(word));
        if (!"undecided".equals(baseForm)) {
            StringTokenizer st = new StringTokenizer(baseForm, ",");
            baseForm = st.nextToken();
            StringBuilder numStr = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            for (int i = 0; i < baseForm.length(); i++) {
                char c = baseForm.charAt(i);
                if ((c <= '9') && (c >= '0')) {
                    numStr.append(c);
                } else {
                    suffix.append(c);
                }
            }
            if (numStr.length() > 0) {
                int cutpos = Integer.parseInt(numStr.toString());
                if (cutpos > result.length()) {
                    cutpos = result.length();
                }
                result = result.substring(0, result.length() - cutpos) + suffix;
            }
        }
        return result;
    }

}
