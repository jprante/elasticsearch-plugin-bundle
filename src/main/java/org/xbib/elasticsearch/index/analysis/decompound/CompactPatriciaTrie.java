package org.xbib.elasticsearch.index.analysis.decompound;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 */
public class CompactPatriciaTrie {

    private static final int EXACT = 0;

    private static final int LOWER = 2;

    private static final String TAB = "\t";

    private static final String NL = "\n";

    private boolean reverse = false;

    private boolean ignorecase = false;

    private double thresh = 0.0;

    private Node root;

    private char[] stringtree;

    private int offset;

    private int basis;

    private int startchar;

    private int endchar;

    private char attentionNumber;

    private char attentionNode;

    private char endOfWordChar;

    public CompactPatriciaTrie() {
        this.root = new Node();
        this.stringtree = null;
        this.startchar = 33;
        this.endchar = 248;
        this.attentionNumber = 2;
        this.attentionNode = 3;
        this.endOfWordChar = 4;
        this.basis = this.endchar - this.startchar + 1;
        this.offset = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
                / Math.log(this.basis));
        this.reverse = false;
        this.ignorecase = false;
    }

    public CompactPatriciaTrie(int sc, int ec, int az, int ak, int eow, boolean rv,
                               boolean ic, char[] stringtree) {
        this.root = null;
        this.stringtree = stringtree;
        this.startchar = sc;
        this.endchar = ec;
        this.attentionNumber = (char) az;
        this.attentionNode = (char) ak;
        this.endOfWordChar = (char) eow;
        this.basis = this.endchar - this.startchar + 1;
        this.offset = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
                / Math.log(this.basis));
        this.reverse = rv;
        this.ignorecase = ic;
    }

    public void train(String word, String classify) {
        train(word, classify, 1);
    }

    public void train(String trainWord, String classify, int nr) {
        String word = trainWord;
        if (root == null) {
            root = getObjectTree(stringtree);
        }
        stringtree = null;
        if (ignorecase) {
            word = word.toLowerCase();
        }
        if (reverse) {
            word = reverse(word);
        }
        Node k = new Node(word + endOfWordChar);
        k.classes(new ArrayList<String>());
        k.classes().add(classify + "=" + nr);
        insert(k);
    }

    public String classify(String word) {
        if (root == null) {
            return classifyString(word);
        }
        return classifyObject(word);
    }

    public void setStartChar(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Character number must be greater than 0");
        } else {
            if (this.root == null) {
                this.root = getObjectTree(this.stringtree);
            }
            this.stringtree = null;
            this.startchar = c;
            this.basis = this.endchar - this.startchar + 1;
            this.offset = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
                    / Math.log(this.basis));
        }
    }

    public void setEndChar(int c) {
        if (c < 0) {
            throw new IllegalArgumentException(
                    "Character number must be greater than 0");
        } else {
            if (this.root == null) {
                this.root = getObjectTree(this.stringtree);
            }
            this.stringtree = null;

            this.endchar = c;
            this.basis = this.endchar - this.startchar + 1;
            this.offset = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
                    / Math.log(this.basis));
        }
    }

    public void setAttentionNumber(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Character number must be greater than 0");
        } else {
            if (this.root == null) {
                this.root = getObjectTree(this.stringtree);
            }
            this.stringtree = null;
            this.attentionNumber = (char) c;
        }
    }

    public void setAttentionNode(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Character number must be greater than 0");
        } else {
            if (this.root == null) {
                this.root = getObjectTree(this.stringtree);
            }
            this.stringtree = null;

            this.attentionNode = (char) c;
        }
    }

    public void setEndOfWordChar(int c) {
        if (c < 0) {
            throw new IllegalArgumentException(
                    "Character number must be greater than 0");
        } else {
            this.endOfWordChar = (char) c;
        }
    }

    private void internalSetStartChar(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Character number must be greater than 0");
        } else {
            this.startchar = c;
            this.basis = this.endchar - this.startchar + 1;
            this.offset = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
                    / Math.log(this.basis));
        }
    }

    private void internalSetEndChar(int c) {
        if (c < 0) {
            throw new IllegalArgumentException(
                    "Character number must be greater than 0");
        } else {
            this.endchar = c;
            this.basis = this.endchar - this.startchar + 1;
            this.offset = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
                    / Math.log(this.basis));
        }
    }

    private void internalSetAttentionNumber(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Character number must be greater than 0");
        } else {
            this.attentionNumber = (char) c;
        }
    }

    private void internalSetAttentionNode(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Character number must be greater than 0");
        } else {
            attentionNode = (char) c;
        }
    }

    public double getThreshold() {
        return this.thresh;
    }

    public void setThreshold(double threshold) {
        this.thresh = threshold;
    }

    public boolean getIgnoreCase() {
        return this.ignorecase;
    }

    public void setIgnoreCase(boolean b) {
        this.ignorecase = b;
    }

    public boolean getReverse() {
        return this.reverse;
    }

    public void setReverse(boolean b) {
        this.reverse = b;
    }

    private String reverse(String s) {
        char[] ret = new char[s.length()];
        StringBuilder torev = new StringBuilder(s);
        for (int i = torev.length() - 1, j = 0; i >= 0; i--, j++) {
            ret[j] = torev.charAt(i);
        }
        return new String(ret);
    }

    private String voted(List<String> classes) {
        if (classes == null) {
            return null;
        }
        int sum = 0;
        int maxval = 0;
        int actval;
        String maxclass = "undecided";
        String actclass;
        for (String cl : classes) {
            StringTokenizer st = new StringTokenizer(cl, "=");
            actclass = st.nextToken();
            if (st.hasMoreTokens()) {
                actval = new Integer(st.nextToken());
            } else {
                actval = 0;
            }
            sum += actval;
            if (actval > maxval) {
                maxval = actval;
                maxclass = actclass;
            }
            if ((actval == maxval) && !actclass.equals(maxclass) && !actclass.isEmpty()) {
                maxclass = new StringBuilder().append(maxclass).append(";").append(actclass).toString();
            }
        }
        if (((double) maxval / (double) sum) >= this.thresh) {
            return maxclass;
        } else {
            return "undecided";
        }
    }

    private List<String> add(List<String> one, List<String> two) {
        List<String> list = new ArrayList<>();
        Map<String, Object> hash = new HashMap<>();
        String clas;
        String snr;
        int nr;
        int nr2;
        String cont;
        for (String s : one) {
            StringTokenizer st = new StringTokenizer(s, "=");
            clas = st.nextToken();
            snr = st.nextToken();
            hash.put(clas, snr);
        }
        for (String s : two) {
            StringTokenizer st = new StringTokenizer(s, "=");
            clas = st.nextToken();
            if (st.hasMoreTokens()) {
                snr = st.nextToken();
                nr = Integer.parseInt(snr);
            } else {
                nr = 0;
            }
            cont = (String) hash.get(clas);
            if (cont != null) {
                nr2 = Integer.parseInt(cont);
                nr += nr2;
            }
            snr = Integer.toString(nr);
            hash.put(clas, snr);
        }
        for (Map.Entry<String,Object> entry : hash.entrySet()) {
            String instr = entry.getKey() + "=" + entry.getValue();
            list.add(instr);
        }
        return list;
    }

    private Node insert(Node k1, Node k2) {
        String w0;
        String w1;
        String w2;
        if (k1 == null) {
            return k2;
        }
        int pos;
        int min;

        if (k1.getContent().length() < k2.getContent().length()) {
            min = k1.getContent().length();
        } else {
            min = k2.getContent().length();
        }
        for (pos = 0; pos < min; pos++) {
            if (k1.getContent().charAt(pos) != k2.getContent().charAt(pos)) {
                break;
            }
        }
        w0 = k2.getContent().substring(0, pos);
        w1 = k1.getContent().substring(pos, k1.getContent().length());
        w2 = k2.getContent().substring(pos, k2.getContent().length());
        if (w2.length() == 0) {
            k1.classes(add(k1.classes(), k2.classes()));
            return k1;
        }
        if (w1.length() == 0) {
            k2.setContent(w2);
            Node goalpos = getChild(k1, w2);
            if (goalpos == null) {
                k1.children().add(k2);
            } else {
                k1.children().remove(goalpos);
                k1.children().add(insert(goalpos, k2));
            }
            k1.classes(add(k1.classes(), k2.classes()));
            return k1;
        } else {
            Node h = new Node(w0);
            k2.setContent(w2);
            h.children().add(k2);
            k1.setContent(w1);
            h.children().add(k1);
            h.classes(add(k1.classes(), k2.classes()));
            return h;
        }
    }

    private Node getChild(Node k, String w) {
        for (Node child : k.children()) {
            if (child.getContent().substring(0, 1).equals(w.substring(0, 1))) {
                return child;
            }
        }
        return null;
    }

    private Node lookup(Node k, String w) {
        Node node;
        String w1;
        String w2;
        if (k == null) {
            return null;
        }
        int min;
        int pos;
        if (k.getContent().length() < w.length()) {
            min = k.getContent().length();
        } else {
            min = w.length();
        }
        for (pos = 0; pos < min; pos++) {
            if (k.getContent().charAt(pos) != w.charAt(pos)) {
                break;
            }
        }
        w1 = k.getContent().substring(pos, k.getContent().length());
        w2 = w.substring(pos, w.length());
        if (w2.length() != 0) {
            node = lookup(getChild(k, w2), w2);
            if (node != null) {
                return node;
            } else {
                return k;
            }
        }
        if (w1.length() != 0) {
            return k;
        }
        return k;
    }

    public void prune() {
        if (root == null) {
            root = getObjectTree(stringtree);
        }
        stringtree = null;
        root = pruneNode(root);
    }

    private Node pruneNode(Node node) {
        String vklass;
        String aklass;
        StringTokenizer st;
        ArrayList<Node> temp;
        if (node.children().isEmpty()) {
            node.setContent(node.getContent().substring(0, 1));
        } else if (node.classes().size() == 1) {
            node.setContent(node.getContent().substring(0, 1));
            node.children().clear();
        } else {
            vklass = voted(node.classes());
            temp = new ArrayList<>();
            for (Node akk : node.children()) {
                if (akk.classes().size() == 1) {
                    aklass = akk.classes().get(0);
                    st = new StringTokenizer(aklass, "=");
                    aklass = st.nextToken();
                    if (!aklass.equals(vklass)) {
                        akk = pruneNode(akk);
                        temp.add(akk);
                    }
                } else {
                    akk = pruneNode(akk);
                    temp.add(akk);
                }
            }
            node.children(temp);
        }
        return node;
    }

    private String classifyString(String s) {
        String word = s;
        if (ignorecase) {
            word = word.toLowerCase();
        }
        if (reverse) {
            word = reverse(word);
        }
        Node k = getNearest(word + endOfWordChar);
        return voted(k.classes());
    }

    private String classifyObject(String s) {
        String word = s;
        if (ignorecase) {
            word = word.toLowerCase();
        }
        if (reverse) {
            word = reverse(word);
        }
        Node k = find(word + endOfWordChar);
        return voted(k.classes());
    }

    public double getProbabilityForClass(String string, String cla) {
        String word = string;
        double ret = 0;
        if (root == null) {
            return getProbabilityForClassString(word, cla);
        }
        if (ignorecase) {
            word = word.toLowerCase();
        }
        if (reverse) {
            word = reverse(word);
        }
        Node k = find(word + "<");
        double valsum = 0;
        double goalval = 0;
        String actclass;
        int actval;
        for (String s : k.classes()) {
            StringTokenizer st = new StringTokenizer(s, "=");
            actclass = st.nextToken();
            actval = new Integer(st.nextToken());
            valsum += actval;
            if (actclass.equals(cla)) {
                goalval = actval;
            }
        }
        if (valsum > 0) {
            ret = goalval / valsum;
        }
        return ret;
    }

    public double getProbabilityForClassString(String string, String cla) {
        String word = string;
        double ret = 0;
        if (this.ignorecase) {
            word = word.toLowerCase();
        }
        if (this.reverse) {
            word = reverse(word);
        }
        Node k = getNearest(word + "<");
        double valsum = 0;
        double goalval = 0;
        String actclass;
        int actval;
        for (String s : k.classes()) {
            StringTokenizer st = new StringTokenizer(s, "=");
            actclass = st.nextToken();
            actval = Integer.parseInt(st.nextToken());
            valsum += actval;
            if (actclass.equals(cla)) {
                goalval = actval;
            }
        }
        if (valsum > 0) {
            ret = goalval / valsum;
        }
        return ret;
    }

    private Node getNearest(String word) {
        return get(word, LOWER);
    }

    private Node get(String word, int mode) {
        String currentWord = word;
        int i = 0;
        StringBuilder currentLabel;
        StringBuilder exlabel = new StringBuilder();
        List<String> currentClasses;
        while (stringtree[i] != attentionNode) {
            exlabel.append(Character.toString(stringtree[i]));
            i++;
        }
        while (true) {
            currentClasses = new ArrayList<>();
            currentLabel = new StringBuilder();
            i++;
            i++;
            while (stringtree[i] != ']') {
                StringBuilder currentClass = new StringBuilder();
                while ((stringtree[i] != ';') && (stringtree[i] != ']')) {
                    currentClass.append(stringtree[i]);
                    i++;
                }
                if (stringtree[i] != ']') {
                    i++;
                }
                currentClasses.add(currentClass.toString());
            }
            if (currentWord.length() == 0) {
                break;
            }
            if ((i + 1) == stringtree.length) {
                if (mode == EXACT) {
                    exlabel .setLength(0);
                    currentClasses = null;
                }
                break;
            }
            i++;
            while (stringtree[i] != currentWord.charAt(0)) {
                if (stringtree[i] == attentionNode) {
                    if (mode == EXACT) {
                        exlabel.setLength(0);
                        currentClasses = null;
                    }
                    break;
                }
                while (stringtree[i] != attentionNumber) {
                    i++;
                }
                i++;
                i += offset;
            }
            if (stringtree[i] == attentionNode) {
                break;
            }
            while (stringtree[i] != attentionNumber) {
                currentLabel.append(stringtree[i]);
                i++;
            }
            i++;
            if (currentLabel.length() > currentWord.length()) {
                if (mode == EXACT) {
                    exlabel.setLength(0);
                    currentClasses = null;
                } else if (mode == LOWER) {
                    exlabel = currentLabel;
                    currentClasses = getClassesAt(string2int(new String(stringtree, i, offset)));
                }
                break;
            }
            String w1 = currentWord.substring(0, currentLabel.length());
            String w2 = currentWord.substring(currentLabel.length());
            if (!(w1.equals(currentLabel.toString()))) {
                if (mode == EXACT) {
                    exlabel.setLength(0);
                    currentClasses = null;
                } else if (mode == LOWER) {
                    exlabel = currentLabel;
                    currentClasses = getClassesAt(string2int(new String(stringtree, i, offset)));
                }
                break;
            }
            int o = string2int(new String(stringtree, i, offset));
            currentWord = w2;
            i = o;
            exlabel = currentLabel;
        }
        Node k = new Node(exlabel.toString());
        k.classes(currentClasses);
        return k;
    }

    private List<String> getClassesAt(int pos) {
        int i = pos;
        List<String> retClasses = new ArrayList<>();
        i++;
        i++;
        while (stringtree[i] != ']') {
            StringBuilder sb = new StringBuilder();
            while ((stringtree[i] != ';') && (stringtree[i] != ']')) {
                sb.append(stringtree[i]);
                i++;
            }
            if (stringtree[i] != ']') {
                i++;
            }
            retClasses.add(sb.toString());
        }
        return retClasses;
    }

    private void insert(Node k) {
        if (root == null) {
            root = getObjectTree(stringtree);
        }
        stringtree = null;
        root.classes(add(root.classes(), k.classes()));
        Node gpos = getChild(root, k.getContent());
        if (gpos == null) {
            gpos = k;
            root.children().add(gpos);
        } else {
            root.children().remove(gpos);
            root.children().add(insert(gpos, k));
        }
    }

    private Node find(String w) {
        if (root == null) {
            root = getObjectTree(stringtree);
        }
        Node wchild = getChild(root, w);
        if (wchild == null) {
            return root;
        } else {
            return lookup(wchild, w);
        }
    }

    private void addStringToMap(Map<String, String> m, char[] treestring, int pos,
                                StringBuilder content) {
        int i = pos;
        i++;
        i++;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (treestring[i] != ']') {
            sb.append(treestring[i]);
            i++;
        }
        sb.append(']');
        i++;
        m.put(content.toString(), sb.toString());
        if (i < treestring.length) {
            while (treestring[i] != this.attentionNode) {
                StringBuilder childContent = new StringBuilder(content);
                StringBuilder childOffset = new StringBuilder();
                while (treestring[i] != this.attentionNumber) {
                    childContent.append(treestring[i]);
                    i++;
                }
                i++;
                for (int j = 0; j < this.offset; j++) {
                    childOffset.append(treestring[i]);
                    i++;
                }
                addStringToMap(m, treestring, string2int(childOffset.toString()), childContent);
            }
        }
    }

    private void addObjectToMap(Map<String, String> m, Node node, StringBuilder content) {
        content.append(node.getContent());
        m.put(content.toString(), formatClasses(node).toString());
        for (Node child : node.children()) {
            addObjectToMap(m, child, new StringBuilder(content.toString()));
        }
    }

    private void addStringToKeySet(Set<String> s, char[] treestring, int pos,
                                   StringBuilder currentContent) {
        int i = pos;
        i++;
        i++;
        while (treestring[i] != ']') {
            i++;
        }
        i++;
        s.add(currentContent.toString());
        if (i < treestring.length) {
            while (treestring[i] != this.attentionNode) {
                StringBuilder aktKindInhalt = new StringBuilder(currentContent);
                StringBuilder aktKindOffset = new StringBuilder();
                while (treestring[i] != this.attentionNumber) {
                    aktKindInhalt.append(treestring[i]);
                    i++;
                }
                i++;
                for (int j = 0; j < this.offset; j++) {
                    aktKindOffset.append(treestring[i]);
                    i++;
                }
                addStringToKeySet(s, treestring, string2int(aktKindOffset
                        .toString()), aktKindInhalt);
            }
        }
    }

    private void addObjectToKeySet(Set<String> s, Node node,
                                   StringBuilder content) {
        content.append(node.getContent());
        s.add(content.toString());
        for (Node aktKind : node.children()) {
            addObjectToKeySet(s, aktKind,
                    new StringBuilder(content.toString()));
        }
    }

    public String getAllEntriesString() {
        StringBuilder ret = new StringBuilder();
        if (this.root != null) {
            addObjectNodesEntriesString(ret, this.root, new StringBuilder());
        } else {
            addStringNodesEntriesString(ret, this.stringtree, 0,
                    new StringBuilder());
        }
        return ret.toString();
    }

    private void addStringNodesEntriesString(StringBuilder s, char[] treestring,
                                             int pos, StringBuilder content) {
        int i = pos;
        i++;
        i++;
        s.append(content);
        s.append(TAB);
        s.append('[');
        while (treestring[i] != ']') {
            s.append(treestring[i]);
            i++;
        }
        s.append(']');
        i++;
        s.append(NL);
        if (i < treestring.length) {
            while (treestring[i] != this.attentionNode) {
                StringBuilder aktKindInhalt = new StringBuilder(content);
                StringBuilder aktKindOffset = new StringBuilder();
                while (treestring[i] != this.attentionNumber) {
                    aktKindInhalt.append(treestring[i]);
                    i++;
                }
                i++;
                for (int j = 0; j < this.offset; j++) {
                    aktKindOffset.append(treestring[i]);
                    i++;
                }
                addStringNodesEntriesString(s, treestring,
                        string2int(aktKindOffset.toString()), aktKindInhalt);
            }
        }
    }

    private void addObjectNodesEntriesString(StringBuilder s, Node node,
                                             StringBuilder content) {
        content.append(node.getContent());
        s.append(content);
        s.append(TAB);
        s.append(formatClasses(node));
        s.append(NL);
        for (Node aktKind : node.children()) {
            addObjectNodesEntriesString(s, aktKind, new StringBuilder(content.toString()));
        }
    }

    private Node getObjectTree(char[] treestring) {
        Node w = new Node("");
        int i = 0;
        StringBuilder tmp = new StringBuilder();
        while (treestring[i] != this.attentionNode) {
            tmp.append(treestring[i]);
            i++;
        }
        if (tmp.length() > 0) {
            w.setContent(tmp.toString());
        }
        List<String> aktclasses = new ArrayList<>();
        i++;
        i++;
        while (treestring[i] != ']') {
            StringBuilder aktclass = new StringBuilder();
            while ((treestring[i] != ';') && (treestring[i] != ']')) {
                aktclass.append(treestring[i]);
                i++;
            }
            if (treestring[i] != ']') {
                i++;
            }
            aktclasses.add(aktclass.toString());
        }
        w.classes(aktclasses);
        w.children(new ArrayList<>());
        i++;
        if (i >= treestring.length) {
            return w;
        }
        while (treestring[i] != this.attentionNode) {
            StringBuilder aktInhalt = new StringBuilder();
            StringBuilder aktOffset = new StringBuilder();
            while (treestring[i] != this.attentionNumber) {
                aktInhalt.append(treestring[i]);
                i++;
            }
            i++;
            for (int j = 0; j < this.offset; j++) {
                aktOffset.append(treestring[i]);
                i++;
            }
            Node aktKind = string2tree(treestring,
                    string2int(aktOffset.toString()));
            aktKind.setContent(aktInhalt.toString());
            w.children().add(aktKind);
        }
        return w;
    }

    private Node string2tree(char[] treestring, int pos) {
        Node w = new Node("");
        int i = pos;
        List<String> aktclasses = new ArrayList<>();
        i++;
        i++;
        while (treestring[i] != ']') {
            StringBuilder aktclass = new StringBuilder();
            while ((treestring[i] != ';') && (treestring[i] != ']')) {
                aktclass.append(treestring[i]);
                i++;
            }
            if (treestring[i] != ']') {
                i++;
            }
            aktclasses.add(aktclass.toString());
        }
        w.classes(aktclasses);
        w.children(new ArrayList<Node>());
        i++;
        if (i >= treestring.length) {
            return w;
        }
        while (treestring[i] != this.attentionNode) {
            StringBuilder aktInhalt = new StringBuilder();
            StringBuilder aktOffset = new StringBuilder();
            while (treestring[i] != this.attentionNumber) {
                aktInhalt.append(treestring[i]);
                i++;
            }
            i++;
            for (int j = 0; j < this.offset; j++) {
                aktOffset.append(treestring[i]);
                i++;
            }
            Node aktKind = string2tree(treestring,
                    string2int(aktOffset.toString()));
            aktKind.setContent(aktInhalt.toString());
            w.children().add(aktKind);
        }
        return w;
    }

    private char[] getStringTree(Node w) {
        StringBuilder ret = new StringBuilder();
        ret.append(w.getContent());
        ret.append(tree2string(w, ret.length()));
        return ret.toString().toCharArray();
    }

    public void save(OutputStream out) throws IOException {
        ObjectOutputStream oos2 = new ObjectOutputStream(out);
        if (this.stringtree == null) {
            this.stringtree = getStringTree(this.root);
        }
        oos2.writeObject("Pretree");
        oos2.writeObject("Stringformat char[]");
        oos2.writeObject("version=1.3");
        oos2.writeObject(this.startchar);
        oos2.writeObject(this.endchar);
        oos2.writeObject((int) this.attentionNumber);
        oos2.writeObject((int) this.attentionNode);
        oos2.writeObject((int) this.endOfWordChar);
        oos2.writeObject(this.reverse);
        oos2.writeObject(this.ignorecase);
        oos2.writeObject(this.stringtree);
        oos2.close();
    }

    private StringBuilder tree2string(Node aktKnoten, int startPos) {
        StringBuilder ret = new StringBuilder();
        ret.append(this.attentionNode);
        int relPos = 1;
        StringBuilder tmp;
        tmp = formatClasses(aktKnoten);
        relPos += tmp.length();
        ret.append(tmp);
        tmp = formatChildrenContents(aktKnoten, relPos);
        relPos += tmp.length();
        ret.append(tmp);
        int vorigerTeilbaum = 0;
        for (Node aktKind : aktKnoten.children()) {
            relPos += vorigerTeilbaum;
            formatPosition(ret, aktKind.getPos(), relPos + startPos);
            tmp = tree2string(aktKind, relPos + startPos);
            vorigerTeilbaum = tmp.length();
            ret.append(tmp);
        }
        return ret;
    }

    private StringBuilder formatClasses(Node node) {
        StringBuilder ret = new StringBuilder("[");
        int k = 0;
        for (String s : node.classes()) {
            k++;
            if (k != 1) {
                ret.append(";");
            }
            ret.append(s);
        }
        ret.append("]");
        return ret;
    }

    private StringBuilder formatChildrenContents(Node node, int startPos) {
        StringBuilder ret = new StringBuilder("");
        int relPos;
        for (Node child : node.children()) {
            ret.append(child.getContent());
            ret.append(this.attentionNumber);
            relPos = ret.length();
            child.setPos(relPos + startPos);
            ret.append(int2string(0));
        }
        return ret;
    }

    private void formatPosition(StringBuilder str, int pos, int offset) {
        str.replace(pos, pos + this.offset, int2string(offset));
    }

    private String int2string(int i) {
        StringBuilder ret = new StringBuilder();
        int rest = i;
        for (int e = this.offset - 1; e >= 0; e--) {
            int k = rest / ((int) (Math.exp(e * Math.log(this.basis))));
            rest = rest % ((int) (Math.exp(e * Math.log(this.basis))));
            char c = (char) (this.startchar + k);
            ret.append(c);
        }
        return ret.toString();
    }

    private int string2int(String s) {
        int ret = 0;
        for (int i = 0; i < this.offset; i++) {
            char c = s.charAt(i);
            int k = (c) - this.startchar;
            ret += k
                    * ((int) Math.exp((this.offset - i - 1)
                    * Math.log(this.basis)));
        }
        return ret;
    }

    public void load(InputStream in) throws IOException {
        load(new ObjectInputStream(in));
    }

    public void load(ObjectInputStream ois) throws IOException {
        try {
            ois.readObject();
            ois.readObject();
            ois.readObject();
            int sc = (Integer) ois.readObject();
            int ec = (Integer) ois.readObject();
            int az = (Integer) ois.readObject();
            int ak = (Integer) ois.readObject();
            int eow = (Integer) ois.readObject();
            boolean rv = (Boolean) ois.readObject();
            boolean ic = (Boolean) ois.readObject();
            char[] st = (char[]) ois.readObject();
            ois.close();
            internalSetStartChar(sc);
            internalSetEndChar(ec);
            internalSetAttentionNumber(az);
            internalSetAttentionNode(ak);
            setEndOfWordChar(eow);
            setReverse(rv);
            setIgnoreCase(ic);
            this.stringtree = st;
            this.root = null;
        } catch (ClassNotFoundException e) {
            // can't happen, we use only primitives
            throw new IllegalArgumentException("class not found", e);
        }
    }
}
