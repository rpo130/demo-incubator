package pr.rpo.util;

public class Pair<F,S> {
    final F first;
    final S second;

    private Pair(F f, S s) {
        this.first = f;
        this.second = s;
    }

    public static <T,K> Pair from(T f, K s) {
        return new Pair(f,s);
    }

    public F first() {
        return first;
    }

    public S senond() {
        return second;
    }

}
