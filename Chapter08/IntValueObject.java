/**
 * 純粋なオブジェクト指向って多分こういう事？
 */
public class IntValueObject {
    private int value;
    public IntValueObject(int value) {
        this.value = value;
    }
    public void f() {this.value *= 10;}
    public void g() {this.value += 30;}
    public void h() {this.value /= 2;}
    public void i() {this.value -= 20;}
    public int get() {
        return this.value;
    }
    public static void main(String ... args) {
        IntValueObject obj = new IntValueObject(5);
        obj.f();
        obj.g();
        obj.h();
        obj.i();
        System.out.println(obj.get());
    }
}
