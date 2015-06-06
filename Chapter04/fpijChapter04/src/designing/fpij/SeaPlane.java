package designing.fpij;

public class SeaPlane extends Vehicle implements FastFly, Sail {
    private int altitude;
    
    @Override
    public void cruise() {
        System.err.print("SeaPlane::crise currently crise like: ");
        
        if (altitude > 0) {
            FastFly.super.cruise();
        } else {
            Sail.super.cruise();
        }
    }
    
}
