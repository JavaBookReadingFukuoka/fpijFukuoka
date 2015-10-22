package designing.fpij;

public interface FastFly extends Fly {
    @Override
    default void takeOff() { System.out.println("FastFly::takeOff"); }
}
