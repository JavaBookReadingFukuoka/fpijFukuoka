package designing.fpij;

import java.util.function.Function;

public class Compose {

    public static void main(String[] args) {
        Function<String, String> target = (String t) -> t.concat("->target");
        Function<String, String> next = (String t) -> t.concat("->next");

        Function<String, String> wrapper = target.compose(next);
        System.out.println(wrapper.apply("input"));

        System.out.println(next.apply(target.apply("input")));
    }
}
