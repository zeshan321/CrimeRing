package brewing;

public class BrewObject {

    public String slot1;
    public String slot2;
    public String slot3;
    public String slot4;
    public String slotC1;
    public String slotC2;
    public String slotC3;
    public String slotC4;
    public String perm = null;
    public int fuel;
    public int duration;
    public long start;

    public BrewObject(String slot1, String slot2, String slot3, String slot4, String slotC1, String slotC2, String slotC3, String slotC4, int fuel, int duration) {
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
        this.slot4 = slot4;
        this.slotC1 = slotC1;
        this.slotC2 = slotC2;
        this.slotC3 = slotC3;
        this.slotC4 = slotC4;
        this.duration = duration;
        this.fuel = fuel;
    }
}
