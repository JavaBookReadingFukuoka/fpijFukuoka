import java.util.stream.Stream;

/**
 * オブジェクトの状態は変化し、状態変更を重ねる・・・って多分こういう事？
 */
public class ObjectWithFunction {
    public static void main(String... args) {
        Stream.of(new IntValueObject(5))
                .map(obj -> new IntValueObject(obj.get() * 10))
                .map(obj -> new IntValueObject(obj.get() + 30))
                .map(obj -> new IntValueObject(obj.get() / 2))
                .map(obj -> new IntValueObject(obj.get() - 20))
                .forEach(o -> System.out.println(o.get()));
    }
}
