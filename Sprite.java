import java.util.ArrayList;
import java.awt.*;

public class Sprite {
    private Location loc;
    private boolean dead;
    private ArrayList<Image> states;
    private int state;
    private int health;
    private String name;

    public Sprite(Location loc, boolean dead, ArrayList<Image> states, int health, int state, String name) {
        this.loc = loc;
        this.dead = dead;
        this.states = states;
        this.health = health;
        this.state = state;
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public String getName() {
        return name;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Location getLoc() {
        return loc;
    }

    public boolean isDead() {
        return dead;
    }

    public Image getState() {
        return states.get(state);
    }

    public void changeState() {
        if (state >= states.size())
            state = 0;
        else
            state++;
    }

    public void changeState(int s) {
        state = s;
    }
}
