package pr.rpo;


import java.util.Arrays;
import java.util.Comparator;

public class Btree {

    //order
    private final static int M = 4;
    private int num;
    private int height;
    private Node head;

    private class Node {
        Pair<Key,Value>[] kv; //M
        int num;
        int level;
        Node father;
        Node[] children;//M+1

        public Node() {
//            this.num=0;
//            this.level=0;
//            this.father = null;
//            this.kv = new Pair[M];
//            this.children = new Node[M+1];
        }

        public int size() {
            return num;
        }

        public boolean isRoot() {
            if(father == null) {
                return true;
            }else {
                return false;
            }
        }

        public boolean isLeaf() {
            if(children == null) {
                return true;
            }else {
                return false;
            }
        }

        public boolean isFull() {
            if(size() >= (M-1)) return true;
            else return false;
        }

        public void insert(Pair<Key,Value> pair) {
            //TODO
            if(kv == null) {
                kv = new Pair[1];
                kv[0] = pair;
            }else {
                Pair<Key,Value>[] tmpKv = Arrays.copyOf(kv,size()+1);
                Arrays.sort(tmpKv, Comparator.comparing((Pair<Key, Value> e) -> e.first));
                this.kv = tmpKv;
            }
        }

        public void remove(Key key) {
            //TODO
            this.kv = Arrays.stream(kv).filter((Pair<Key,Value> e) -> e.first.compareTo(key)!=0).toArray(Pair[]::new);
        }

        public Value get(Key key) {
            for(Pair<Key,Value> p : kv) {
                if(p.first.equals(key)) {
                    return p.second;
                }
            }
            return null;
        }

        public boolean contain(Key key) {
            if(get(key) != null) {
                return true;
            }else {
                return false;
            }
        }

        public Pair<Pair<Key, Value>, Node> split(int index) {
            Pair selePair = kv[index];

            this.kv = Arrays.copyOf(kv,index);
            //TODO
            return null;
        }


    }

    private class Key implements Comparable<Key> {
        String s;

        public String get() {
            return this.s;
        }

        public void set(String s) {
            this.s = s;
        }

        @Override
        public int compareTo(Key o) {
            return s.compareTo(o.s);
        }
    }

    private class Value {
        String s;

        public String get() {
            return this.s;
        }

        public void set(String s) {
            this.s = s;
        }
    }

    private static class Pair<F,S> {
        F first;
        S second;
        public Pair(F f, S s) {
            this.first = f;
            this.second = s;
        }

        public static <T,K> Pair from(T f, K s) {
            return new Pair<T, K>(f,s);
        }

        public F first() {
            return first;
        }

        public S senond() {
            return second;
        }
    }

    public void add(Pair<Key, Value> pair) {
        //TODO
    }

    public Value get(Key key) {
        //TODO
        return null;
    }

    public int size() {
        //TODO
        return 0;
    }

    public int height() {
        //TODO
        return 0;
    }

    public String prettyPrintString() {
        //TODO
        return "";
    }

    private void split() {
        //TODO
    }

    private void merge() {
        //TODO
    }

}
