package pr.rpo;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class Btree {

    //order
    private int M;
    private int num;
    private int height;
    private Node head;

    private static class Node {
        private int order;
        private Pair<Key,Value>[] kv; //M
        private int num;
        private Node father;
        private Node[] children;//M+1

        public Node() {
            this.order = 4;//default
            this.kv = new Pair[0];
            this.children = new Node[0];
        }

        public Node(int order) {
            this.order = order;
            this.kv = new Pair[0];
            this.children = new Node[0];
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
            if(children == null || children.length == 0) {
                return true;
            }else {
                return false;
            }
        }

        public boolean isFull() {
            if(size() >= (order-1)) return true;
            else return false;
        }

        public boolean needSplit() {
            if(size() >= order) return true;
            else return false;
        }

        public void insert(Pair<Key,Value> pair) {
            if(kv == null) {
                kv = new Pair[1];
                kv[0] = pair;
            }else {
                Pair<Key,Value>[] tmpKv = Arrays.copyOf(kv,size()+1);
                tmpKv[size()] = pair;
                Arrays.sort(tmpKv, Comparator.comparing((Pair<Key, Value> e) -> e.first()));
                this.kv = tmpKv;
            }
            this.num++;
        }

        public void insertAt(int index,Pair<Key, Value> pair) {
            assert index <= num;
            assert index >= 0;

            Pair<Key,Value>[] tmpKv = Arrays.copyOf(kv,size()+1);
            for(int i = tmpKv.length-1; i > index; i--) {
                tmpKv[i] = tmpKv[i-1];
            }
            tmpKv[index] = pair;
            this.kv = tmpKv;
            this.num++;
        }

        public void remove(Key key) {
            this.kv = Arrays.stream(kv).filter((Pair<Key,Value> e) -> e.first().compareTo(key)!=0).toArray(Pair[]::new);
        }

        public void removeAt(int index) {
            assert index <= num;
            assert index >= 0;

            Pair<Key,Value> item = kv[index];
            for (int i = index; i < num-1; i++) {
                kv[i] = kv[i+1];
            }
        }

        public Value get(Key key) {
            for(Pair<Key,Value> p : kv) {
                if(p.first().compareTo(key) == 0) {
                    return p.second();
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

        public int findInsertPointIndex(Key key) {
            if (num <= 0) return 0;

            for(int i=0; i < num; i++) {
                if(kv[i].first().compareTo(key) > 0) {
                    return i;
                }
            }

            return num;
        }

        // -1 represent not exist
        public int findIndex(Key key) {
            if(num <= 0) return -1;

            for(int i=0; i < num; i++) {
                if(kv[i].first().compareTo(key) == 0) {
                    return i;
                }
            }

            return -1;
        }

        public int findNextChildIndex(Key key) {
            if(num <= 0) return -1;

            for(int i=0; i < num; i++) {
                if(kv[i].first().compareTo(key) > 0) {
                    return i;
                }
            }

            return num;
        }

        public void insertChildAt(int index, Node node) {
            assert index <= num+1;
            assert index >= 0;

            Node[] child = Arrays.copyOf(this.children,size() + 1);
            for(int i = child.length-1; i>index; i-- ) {
                child[i] = child[i-1];
            }
            child[index] = node;
            this.children = child;
        }

        public Node removeChildAt(int index) {
            assert index <= num+1;
            assert index >= 0;

            Node child = children[index];
            for (int i = index; i < num+1; i++) {
                children[i] = children[i+1];
            }
            return child;
        }

        public Node getChildAt(int index) {
            assert index <= num+1;
            assert index >= 0;

            return children[index];
        }

        public Pair<Pair<Key, Value>, Node> splitAt(int index) {
            int len = index;
            Pair selePair = kv[index];

            Node newNode = (Node)Copy.deepCopy(this);

            newNode.kv = Arrays.copyOfRange(newNode.kv,index+1,newNode.num);
            if(isLeaf()) {
                newNode.children = new Node[0];
            }else {
                newNode.children = Arrays.copyOfRange(newNode.children, index + 1, newNode.num + 1);
                newNode.updateChildrenFatherField();
            }

            newNode.num = this.num-(index+1);

            this.kv = Arrays.copyOf(kv,len);
            if(isLeaf()) {
                newNode.children = new Node[0];
            }else {
                this.children = Arrays.copyOf(this.children,len+1);
                this.updateChildrenFatherField();
            }
            this.num = len;

            return Pair.from(selePair,newNode);
        }

        private void updateChildrenFatherField() {
            for(Node n : children) {
                n.father = this;
            }
        }

        public String prettyPrint() {
            return prettyPrint("*");
        }

        private String prettyPrint(String prefix) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append("Node(" + this  + ")").append(System.lineSeparator())
              .append(prefix).append("size():" + this.size()).append(System.lineSeparator())
              .append(prefix).append(Arrays.stream(kv).filter(Objects::nonNull).map(e -> e.first().get()+":"+e.second().get()).collect(Collectors.joining( System.lineSeparator() + prefix))).append(System.lineSeparator());
            if(!isLeaf()) {
                sb.append(Arrays.stream(children).filter(Objects::nonNull).map(e -> e.prettyPrint(prefix + "*")).collect(Collectors.joining()));
            }
            return sb.toString();
        }
    }

    private static class Key implements Comparable<Key> {
        private String s;

        public Key() {}

        public Key(String s) {
            this.s = s;
        }

        public String get() {
            return this.s;
        }

        public void set(String s) {
            this.s = s;
        }

        @Override
        public int compareTo(Key o) {
            return s.compareTo(o.get());
        }
    }

    private static class Value {
        String s;

        public Value() {}

        public Value(String s) {
            this.s = s;
        }
        public String get() {
            return this.s;
        }

        public void set(String s) {
            this.s = s;
        }
    }

    public void add(Pair<Key, Value> pair) {
        if(head == null) {
            head = new Node();
            this.height++;
        }

        Node node = head;
        while(!node.isLeaf()) {
            if(node.contain(pair.first())) return;

            int i = node.findNextChildIndex(pair.first());
            node = node.getChildAt(i);
        }

        if(node.contain(pair.first())) return;

        if(node.isFull()) {
            node.insert(pair);
            while (node.needSplit()) {
                Pair<Pair<Key,Value>,Node> splitPair = node.splitAt(M/2);
                if(node.father == null) {
                    Node newHead = new Node();
                    newHead.insertAt(0,splitPair.first());
                    newHead.insertChildAt(0,splitPair.second());
                    newHead.insertChildAt(0,node);

                    newHead.updateChildrenFatherField();

                    this.head = newHead;
                    this.height++;
                    node = node.father;
                }else {
                    node = node.father;

                    int i = node.findInsertPointIndex(splitPair.first().first());
                    node.insertAt(i,splitPair.first());
                    node.insertChildAt(i+1,splitPair.second());
                    node.updateChildrenFatherField();
                }


            }
        }else {
            node.insert(pair);
        }
        this.num++;
    }

    public Value get(Key key) {
        if(head == null) {
            return null;
        }
        return get(head, key);
    }

    private Value get(Node node, Key key) {
        if(node.contain(key)) return node.get(key);

        int childIndex = node.findNextChildIndex(key);
        if(childIndex != -1) return get(node.children[childIndex],key);
        else return null;
    }

    public Btree() {
        this.M = 4;
    }

    public Btree(int order) {
        this.M = order;
    }

    public int size() {
        return this.num;
    }

    public int height() {
        return this.height;
    }

    public String prettyPrintString() {
        if(head == null) {
            return "btree is empty";
        }else {
            return head.prettyPrint();
        }
    }

    private void merge() {
        //TODO
    }

    public static void main(String[] args) {
        Btree st = new Btree();

        st.add(Pair.from(new Key("www.cs.princeton.edu"), new Value("128.112.136.12")));
        st.add(Pair.from(new Key("www.cs.princeton.edu"), new Value("128.112.136.11")));
        st.add(Pair.from(new Key("www.princeton.edu"), new Value("128.112.128.15")));
        st.add(Pair.from(new Key("www.yale.edu"), new Value("130.132.143.21")));
        st.add(Pair.from(new Key("www.simpsons.com"), new Value("209.052.165.60")));
        st.add(Pair.from(new Key("www.apple.com"), new Value("17.112.152.32")));
        st.add(Pair.from(new Key("www.amazon.com"), new Value("207.171.182.16")));
        st.add(Pair.from(new Key("www.ebay.com"), new Value("66.135.192.87")));
        st.add(Pair.from(new Key("www.cnn.com"), new Value( "64.236.16.20")));
        st.add(Pair.from(new Key("www.google.com"), new Value("216.239.41.99")));
        st.add(Pair.from(new Key("www.nytimes.com"), new Value("199.239.136.200")));
        st.add(Pair.from(new Key("www.microsoft.com"), new Value("207.126.99.140")));
        st.add(Pair.from(new Key("www.dell.com"), new Value("143.166.224.230")));
        st.add(Pair.from(new Key("www.slashdot.org"), new Value("66.35.250.151")));
        st.add(Pair.from(new Key("www.espn.com"), new Value("199.181.135.201")));
        st.add(Pair.from(new Key("www.weather.com"), new Value("63.111.66.11")));
        st.add(Pair.from(new Key("www.yahoo.com"), new Value("216.109.118.65")));

        System.out.println("size:    " + st.size());
        System.out.println("height:  " + st.height());
        System.out.println();
        System.out.println(st.prettyPrintString());
    }

}
