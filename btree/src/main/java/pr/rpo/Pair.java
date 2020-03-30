package pr.rpo;

public class Pair<F,S> {
    private F first;
    private S second;

    private Pair() {}

    private Pair(F f, S s) {
        this.first = f;
        this.second = s;
    }

    public static <T,K> Pair from(T f, K s) {
        return new Pair<T, K>(f,s);
    }

    public F first() {
        return (F)first;
    }

    public S second() {
        return (S)second;
    }

    public static void main(String[] args) {
        Pair<String, String> pair = Pair.from("a", "b");

        Pair<String, String> copyPair = (Pair<String, String>) Copy.deepCopy(pair);

        System.out.println(copyPair.first);
        System.out.println(copyPair.second);
    }
}
