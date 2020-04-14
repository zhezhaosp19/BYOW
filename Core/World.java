package byow.Core;

import byow.TileEngine.TETile;

//import java.awt.geom.Point2D;
import java.io.Serializable;

public class World implements Serializable {
    private TETile[][] world;
    private Position avatar;

    public World(TETile[][] w, Position a) {
        this.world = w;
        this.avatar = a;
    }

    public TETile[][] world() {
        return this.world;
    }

    public Position avatar() {
        return this.avatar;
    }
}
