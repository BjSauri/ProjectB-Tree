package ru.itis.unfa11503;

import java.util.*;

public class BTree {
    private class Node {
        int[] keys;
        int t;
        Node[] children;
        int n;
        boolean leaf;

        Node(int t, boolean leaf) {
            this.t = t;
            this.leaf = leaf;
            this.keys = new int[2 * t - 1];
            this.children = new Node[2 * t];
            this.n = 0;
        }
    }

    public static class Stats {
        public long operations;
        public long nanos;

        public Stats(long operations, long nanos) {
            this.operations = operations;
            this.nanos = nanos;
        }
    }

    private Node root;
    private final int t;
    private long ops;

    public BTree(int t) {
        if (t < 2) throw new IllegalArgumentException("t must be >= 2");
        this.t = t;
    }

    private void resetOps() {
        ops = 0;
    }

    public Stats insertMeasured(int key) {
        resetOps();
        long start = System.nanoTime();
        insert(key);
        return new Stats(ops, System.nanoTime() - start);
    }

    public Stats searchMeasured(int key) {
        resetOps();
        long start = System.nanoTime();
        search(key);
        return new Stats(ops, System.nanoTime() - start);
    }

    public Stats deleteMeasured(int key) {
        resetOps();
        long start = System.nanoTime();
        delete(key);
        return new Stats(ops, System.nanoTime() - start);
    }

    public boolean search(int key) {
        return search(root, key) != null;
    }

    private Node search(Node x, int key) {
        if (x == null) return null;
        int i = 0;
        while (i < x.n && key > x.keys[i]) {
            i++;
            ops++;
        }
        if (i < x.n && key == x.keys[i]) {
            ops++;
            return x;
        }
        if (x.leaf) return null;
        ops++;
        return search(x.children[i], key);
    }

    public void insert(int key) {
        if (root == null) {
            root = new Node(t, true);
            root.keys[0] = key;
            root.n = 1;
            ops++;
            return;
        }

        if (root.n == 2 * t - 1) {
            Node s = new Node(t, false);
            s.children[0] = root;
            splitChild(s, 0, root);
            int i = 0;
            if (s.keys[0] < key) i++;
            insertNonFull(s.children[i], key);
            root = s;
        } else {
            insertNonFull(root, key);
        }
    }

    private void insertNonFull(Node x, int key) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && key < x.keys[i]) {
                x.keys[i + 1] = x.keys[i];
                i--;
                ops++;
            }
            x.keys[i + 1] = key;
            x.n++;
            ops++;
        } else {
            while (i >= 0 && key < x.keys[i]) {
                i--;
                ops++;
            }
            i++;
            if (x.children[i].n == 2 * t - 1) {
                splitChild(x, i, x.children[i]);
                if (key > x.keys[i]) i++;
            }
            insertNonFull(x.children[i], key);
        }
    }

    private void splitChild(Node parent, int i, Node y) {
        Node z = new Node(t, y.leaf);
        z.n = t - 1;

        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
            ops++;
        }

        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
                ops++;
            }
        }

        y.n = t - 1;

        for (int j = parent.n; j >= i + 1; j--) parent.children[j + 1] = parent.children[j];
        parent.children[i + 1] = z;

        for (int j = parent.n - 1; j >= i; j--) parent.keys[j + 1] = parent.keys[j];
        parent.keys[i] = y.keys[t - 1];
        parent.n++;
    }

    public void delete(int key) {
        if (root == null) return;
        delete(root, key);
        if (root.n == 0) {
            root = root.leaf ? null : root.children[0];
        }
    }

    private void delete(Node x, int key) {
        int idx = findKey(x, key);

        if (idx < x.n && x.keys[idx] == key) {
            if (x.leaf) deleteFromLeaf(x, idx);
            else deleteFromNonLeaf(x, idx);
        } else {
            if (x.leaf) return;

            boolean flag = idx == x.n;

            if (x.children[idx].n < t) fill(x, idx);

            if (flag && idx > x.n) delete(x.children[idx - 1], key);
            else delete(x.children[idx], key);
        }
    }

    private int findKey(Node x, int key) {
        int idx = 0;
        while (idx < x.n && x.keys[idx] < key) {
            idx++;
            ops++;
        }
        return idx;
    }

    private void deleteFromLeaf(Node x, int idx) {
        for (int i = idx + 1; i < x.n; i++) x.keys[i - 1] = x.keys[i];
        x.n--;
        ops++;
    }

    private void deleteFromNonLeaf(Node x, int idx) {
        int k = x.keys[idx];

        if (x.children[idx].n >= t) {
            int pred = getPred(x, idx);
            x.keys[idx] = pred;
            delete(x.children[idx], pred);
        } else if (x.children[idx + 1].n >= t) {
            int succ = getSucc(x, idx);
            x.keys[idx] = succ;
            delete(x.children[idx + 1], succ);
        } else {
            merge(x, idx);
            delete(x.children[idx], k);
        }
    }

    private int getPred(Node x, int idx) {
        Node cur = x.children[idx];
        while (!cur.leaf) cur = cur.children[cur.n];
        return cur.keys[cur.n - 1];
    }

    private int getSucc(Node x, int idx) {
        Node cur = x.children[idx + 1];
        while (!cur.leaf) cur = cur.children[0];
        return cur.keys[0];
    }

    private void fill(Node x, int idx) {
        if (idx != 0 && x.children[idx - 1].n >= t) borrowFromPrev(x, idx);
        else if (idx != x.n && x.children[idx + 1].n >= t) borrowFromNext(x, idx);
        else {
            if (idx != x.n) merge(x, idx);
            else merge(x, idx - 1);
        }
    }

    private void borrowFromPrev(Node x, int idx) {
        Node child = x.children[idx];
        Node sibling = x.children[idx - 1];

        for (int i = child.n - 1; i >= 0; i--) child.keys[i + 1] = child.keys[i];
        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) child.children[i + 1] = child.children[i];
        }

        child.keys[0] = x.keys[idx - 1];
        if (!child.leaf) child.children[0] = sibling.children[sibling.n];
        x.keys[idx - 1] = sibling.keys[sibling.n - 1];

        child.n++;
        sibling.n--;
        ops++;
    }

    private void borrowFromNext(Node x, int idx) {
        Node child = x.children[idx];
        Node sibling = x.children[idx + 1];

        child.keys[child.n] = x.keys[idx];
        if (!child.leaf) child.children[child.n + 1] = sibling.children[0];
        x.keys[idx] = sibling.keys[0];

        for (int i = 1; i < sibling.n; i++) sibling.keys[i - 1] = sibling.keys[i];
        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) sibling.children[i - 1] = sibling.children[i];
        }

        child.n++;
        sibling.n--;
        ops++;
    }

    private void merge(Node x, int idx) {
        Node child = x.children[idx];
        Node sibling = x.children[idx + 1];

        child.keys[t - 1] = x.keys[idx];

        for (int i = 0; i < sibling.n; i++) child.keys[i + t] = sibling.keys[i];
        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) child.children[i + t] = sibling.children[i];
        }

        for (int i = idx + 1; i < x.n; i++) x.keys[i - 1] = x.keys[i];
        for (int i = idx + 2; i <= x.n; i++) x.children[i - 1] = x.children[i];

        child.n += sibling.n + 1;
        x.n--;
        ops++;
    }
}
