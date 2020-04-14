package byow.Core;

//import byow.SaveDemo.Editor;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
//import byow.Core.World;
//import org.junit.Test;
//import edu.princeton.cs.algs4.ST;
//
//
//import javax.imageio.plugins.tiff.TIFFDirectory;
//import java.awt.*;
//import java.awt.font.TextLayout;
//import java.io.PipedOutputStream;
import java.awt.*;
//import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;



public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private long SEED;
    private Random RANDOM = new Random(100);
    private HashSet<Room> rooms = new HashSet<>();
    private LinkedList<Room> unconnectedRooms = new LinkedList<>();
    //    private TETile[][] finalWorldFrame;
    private World finalWorld = new World(new TETile[WIDTH][HEIGHT], new Position(0, 0));
    private LinkedList<Position> upperLeftCorners = new LinkedList<>();
    private KDTree upperLeftTree;
    private HashMap<Position, Room> cornerRoom = new HashMap<>();

    private BufferedImage lightImage;
    private ArrayList<Light> lights = new ArrayList<>();



    private class Orientation {
        private boolean horizontal = true;
        private boolean increase = true;

        private Orientation(boolean h, boolean i) {
            horizontal = h;
            increase = i;
        }
    }

    private class Corner {
        private Position position;
        private Orientation orientation;

        private Corner(Position p, Orientation o) {
            position = p;
            orientation = o;
        }
    }

    private class Room {
        private Position upperLeft;
        private Position lowerRight;
        private boolean connected;

        private Room(Position ul, Position lr, Boolean c) {
            upperLeft = ul;
            lowerRight = lr;
            connected = c;
        }
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT);
        showMenu();



        ter.renderFrame(finalWorld.world());

    }

    private void showMenu() {
//        StdAudio.loop("mario.wav");
        StdDraw.clear();
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.pink);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Game Begins.");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 7, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 9, "Quit Game (:Q)");

        StdDraw.show();
        Font font2 = new Font("Monaco", Font.PLAIN, 15);
        StdDraw.setFont(font2);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character nextKey = StdDraw.nextKeyTyped();
                if (nextKey.equals('n') || nextKey.equals('N')) {
                    String seed = readSeed();
                    Position p = randomPosition(WIDTH, 0);
                    String s = seed.substring(1, seed.length() - 1);
                    SEED = Long.parseLong(s);
                    finalWorld = new World(generateNewWorld(SEED), p);
                    addAvatar(finalWorld, p);
                    playGame();
                    return;
                } else if (nextKey.equals('l') || nextKey.equals('L')) {
                    finalWorld = loadGame();
                    ter.initialize(WIDTH, HEIGHT);
                    ter.renderFrame(finalWorld.world());
                    playGame();
                    return;
                } else if (nextKey.equals(':')) {
                    while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                            Character nextKeyQ = StdDraw.nextKeyTyped();
                            if (!nextKeyQ.equals('q') && !nextKey.equals('Q')) {
                                continue;
                            } else {
                                System.exit(0);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private String readSeed() {
        StringBuilder seed = new StringBuilder();
        seed.append('n');
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character nextKey = StdDraw.nextKeyTyped();
                if (!nextKey.equals('s') && !nextKey.equals('S')) {
                    if (nextKey > '9' || nextKey < '0') {
                        throw new IllegalArgumentException();
                    }
                    seed.append(nextKey);
                } else {
                    seed.append(nextKey);
                    String s = seed.toString();
                    return s;
                }
            }
        }
    }


    private void playGame() {
        ter.renderFrame(finalWorld.world());
        addLight();
//        ter.renderFrame(finalWorld.world);
        while (true) {
            mouse(finalWorld);
            if (StdDraw.hasNextKeyTyped()) {
                Character nextKey = StdDraw.nextKeyTyped();
                if (nextKey.equals('q') || nextKey.equals('Q')) {
                    saveGame(finalWorld);
                    System.exit(0);
                    return;
                }
                checkMove(nextKey);
            }
        }
    }

    private void checkMove(Character input) {
        if (!input.equals(':')) {
            int x = finalWorld.avatar().getX();
            int y = finalWorld.avatar().getY();
            switch (input) {
                case 'W':
                case 'w':
                    if (finalWorld.world()[x][y + 1].equals(Tileset.WALL)
                            || finalWorld.world()[x][y + 1].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
//                                StdAudio.play("crash.wav");
                    } else {
                        finalWorld.world()[x][y + 1] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x, y + 1));
//                                StdAudio.play("pop.wav");
                    }
                    ter.renderFrame(finalWorld.world());
                    break;
                case 'S':
                case 's':
                    if (finalWorld.world()[x][y - 1].equals(Tileset.WALL)
                            || finalWorld.world()[x][y - 1].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
//                                StdAudio.play("crash.wav");
                    } else {
                        finalWorld.world()[x][y - 1] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x, y - 1));
//                                StdAudio.play("pop.wav");
                    }
                    ter.renderFrame(finalWorld.world());
                    break;
                case 'A':
                case 'a':
                    if (finalWorld.world()[x - 1][y].equals(Tileset.WALL)
                            || finalWorld.world()[x - 1][y].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
//                                StdAudio.play("crash.wav");
                    } else {
                        finalWorld.world()[x - 1][y] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x - 1, y));
//                                StdAudio.play("pop.wav");
                    }
                    ter.renderFrame(finalWorld.world());
                    break;
                case 'D':
                case 'd':
                    if (finalWorld.world()[x + 1][y].equals(Tileset.WALL)
                            || finalWorld.world()[x + 1][y].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
//                                StdAudio.play("crash.wav");
                    } else {
                        finalWorld.world()[x + 1][y] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x + 1, y));
//                                StdAudio.play("pop.wav");
                    }
                    ter.renderFrame(finalWorld.world());
                    break;
                default:
                    break;
            }
        } else {
            while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    Character nextKeyQ = StdDraw.nextKeyTyped();
                    if (!nextKeyQ.equals('q') && !nextKeyQ.equals('Q')) {
                        continue;
                    } else {
                        saveGame(finalWorld);
                        System.exit(0);
                        return;
                    }
                }
            }
        }
    }

    private void mouse(World world) {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        if (world.world()[x][y].equals(Tileset.FLOOR)) {
            ter.renderFrame(world.world());
            StdDraw.setPenColor(Color.pink);
            StdDraw.text(2, HEIGHT - 1, "FLOOR");
        } else if (world.world()[x][y].equals(Tileset.NOTHING)) {
            ter.renderFrame(world.world());
            StdDraw.setPenColor(Color.pink);
            StdDraw.text(2, HEIGHT - 1, "NOTHING");
        } else if (world.world()[x][y].equals(Tileset.AVATAR)) {
            ter.renderFrame(world.world());
            StdDraw.setPenColor(Color.pink);
            StdDraw.text(2, HEIGHT - 1, "AVATAR");
        } else {
            ter.renderFrame(world.world());
            StdDraw.setPenColor(Color.pink);
            StdDraw.text(2, HEIGHT - 1, "WALL");
        }
        StdDraw.show();
    }

    private void addLight() {
        lights.add(new Light(5, 5, 3, 12));
        lights.add(new Light(WIDTH / 5, HEIGHT / 3, 6, 12));
        lights.add(new Light(WIDTH / 3, HEIGHT / 5, 8, 12));
        lights.add(new Light(WIDTH / 2, HEIGHT / 4, 5, 12));
        lights.add(new Light(WIDTH / 10, HEIGHT / 8, 6, 12));

        lightImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) lightImage.getGraphics();
        g.setColor(new Color(0, 0, 0, 225));
        g.fillRect(0, 0, lightImage.getWidth(), lightImage.getHeight());

        Composite old = g.getComposite();

        for (Light l : lights) {
            l.render(g);
        }

        g.dispose();
    }

    private void addAvatar(World world, Position p) {
        while (!world.world()[p.getX()][p.getY()].equals(Tileset.FLOOR)) {
            p = randomPosition(WIDTH, 0);
        }
        world.world()[p.getX()][p.getY()] = Tileset.AVATAR;
        finalWorld = new World(world.world(), p);
    }

    private World loadGame() {
        File f = new File("./save_data.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (World) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
//                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
//                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
//                System.exit(0);
            }
        }

        /* In the case no Editor has been saved yet, we return a new one. */
        return new World(new TETile[WIDTH][HEIGHT], new Position(0, 0));
    }

    private static void saveGame(World world) {
        File f = new File("./save_data.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(world);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
//            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
//            System.exit(0);
        }
    }


    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        finalWorld = new World(new TETile[WIDTH][HEIGHT], finalWorld.avatar());

        LinkedList<Character> strQueue = new LinkedList<>();
        for (char c : input.toCharArray()) {
            strQueue.offer(c);
        }

        Character nextKey = strQueue.remove();
        if (nextKey.equals('N') || nextKey.equals('n')) {
            StringBuilder seedBuilder = new StringBuilder();
            seedBuilder.append(nextKey);
            nextKey = strQueue.remove();
            while (!nextKey.equals('s') && !nextKey.equals('S')) {
                seedBuilder.append(nextKey);
                nextKey = strQueue.remove();
            }
            seedBuilder.append(nextKey);

            String seed = seedBuilder.toString();
            String s = seed.substring(1, seed.length() - 1);
            SEED = Long.parseLong(s);
            Position p = randomPosition(WIDTH, 0);
            finalWorld = new World(generateNewWorld(SEED), p);
            addAvatar(finalWorld, p);

            if (strQueue.isEmpty()) {
                return finalWorld.world();
            }

            playGameInput(strQueue);
            return finalWorld.world();

        } else if (nextKey.equals('l') || nextKey.equals('L')) {
            finalWorld = loadGame();
            playGameInput(strQueue);
            return finalWorld.world();

        } else if (nextKey.equals(':')) {
            while (!nextKey.equals('q') && !nextKey.equals('Q')) {
                nextKey = strQueue.remove();
            }
            return finalWorld.world();
        } else {
            return finalWorld.world();
        }
    }

    private void playGameInput(LinkedList<Character> strQueue) {
        while (!strQueue.isEmpty() && !strQueue.peekFirst().equals(':')) {
            Character nextKey = strQueue.remove();
            int x = finalWorld.avatar().getX();
            int y = finalWorld.avatar().getY();
            switch (nextKey) {
                case 'W':
                case 'w':
                    if (finalWorld.world()[x][y + 1].equals(Tileset.WALL)
                            || finalWorld.world()[x][y + 1].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
                    } else {
                        finalWorld.world()[x][y + 1] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x, y + 1));
                    }
                    break;
                case 'S':
                case 's':
                    if (finalWorld.world()[x][y - 1].equals(Tileset.WALL)
                            || finalWorld.world()[x][y - 1].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
                    } else {
                        finalWorld.world()[x][y - 1] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x, y - 1));
                    }
                    break;
                case 'A':
                case 'a':
                    if (finalWorld.world()[x - 1][y].equals(Tileset.WALL)
                            || finalWorld.world()[x - 1][y].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
                    } else {
                        finalWorld.world()[x - 1][y] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x - 1, y));
                    }
                    break;
                case 'D':
                case 'd':
                    if (finalWorld.world()[x + 1][y].equals(Tileset.WALL)
                            || finalWorld.world()[x + 1][y].equals(Tileset.NOTHING)) {
                        System.out.print("Cannot be wall.");
                    } else {
                        finalWorld.world()[x + 1][y] = Tileset.AVATAR;
                        finalWorld.world()[x][y] = Tileset.FLOOR;
                        finalWorld = new World(finalWorld.world(), new Position(x + 1, y));
                    }
                    break;
                default:
                    break;
            }
        }

        if (strQueue.isEmpty()) {
            return;
        }
        Character nextKey = strQueue.remove();
        while (!nextKey.equals('q') && !nextKey.equals('Q') && !strQueue.isEmpty()) {
            nextKey = strQueue.remove();
        }
        if (nextKey.equals('q') || nextKey.equals('Q')) {
            saveGame(finalWorld);
            return;
        }
        if (strQueue.isEmpty()) {
            return;
        }
    }


    private TETile[][] generateNewWorld(long seed) {
        RANDOM = new Random(seed);
        for (int i = 0; i < HEIGHT; i += 1) {
            Position startp = new Position(0, i);
            addRow(finalWorld.world(), startp, WIDTH, Tileset.NOTHING);
        }

        Room centerRoom = addFirstRoom(finalWorld.world());
        // should add random nums rooms!!!
        int numOfRooms = RANDOM.nextInt(50) + 20;
        addNumberRooms(finalWorld.world(), numOfRooms);
        upperLeftTree = new KDTree(upperLeftCorners);
        connectWithHallways(finalWorld.world(), centerRoom);

        return finalWorld.world();
    }

    private Room addFirstRoom(TETile[][] world) {
        int startX = RANDOM.nextInt(6) + WIDTH / 2 - 3; //upper: width / 2 + 3; lower: width / 2 - 3
        int startY = RANDOM.nextInt(6) + HEIGHT / 2 - 3;
        Position startP = new Position(startX, startY);
        int w = RANDOM.nextInt(WIDTH / 2 - 9) + 4;
        int h = RANDOM.nextInt(HEIGHT / 2 - 9) + 4;
        Room centerRoom = addRoom(finalWorld.world(), startP, w, h);
        rooms.add(centerRoom);
        unconnectedRooms.add(centerRoom);
        Position centerULCorner = centerRoom.upperLeft;
        upperLeftCorners.add(centerULCorner);
        cornerRoom.put(centerULCorner, centerRoom);
        return centerRoom;
    }

    private void addNumberRooms(TETile[][] world, int num) {
        for (int i = 0; i < num; i += 1) {
            int w = RANDOM.nextInt(WIDTH / 3 - 4) + 4; // lower: 4, upper:width / 3
            int h = RANDOM.nextInt(HEIGHT / 3 - 4) + 4;
            Position randomP = randomPosition(WIDTH - w + 1, h);
            if (!overlap(finalWorld.world(), randomP, w, h)) {
                Room newlyAddedRoom = addRoom(world, randomP, w, h);
                rooms.add(newlyAddedRoom);
                unconnectedRooms.add(newlyAddedRoom);
                upperLeftCorners.add(newlyAddedRoom.upperLeft);
                cornerRoom.put(randomP, newlyAddedRoom);
            }
        }
    }


    private void connectWithHallways(TETile[][] world, Room rootRoom) {
        if (upperLeftCorners.isEmpty()) {
            return;
        } else {
            Room closest = findClosestRoom(rootRoom);
            connect2Rooms(world, rootRoom, closest);
            upperLeftCorners.remove(closest.upperLeft);
            upperLeftTree = new KDTree(upperLeftCorners);
            connectWithHallways(world, closest);
        }
    }

    private Room findClosestRoom(Room r1) {
        Position ul2 = upperLeftTree.nearest(r1.upperLeft.getX(), r1.upperLeft.getY());
        return cornerRoom.get(ul2);
    }

    private void connect2Rooms(TETile[][] world, Room r1, Room r2) {
        int ulX1 = r1.upperLeft.getX();
        int ulY1 = r1.upperLeft.getY();
        int ulX2 = r2.upperLeft.getX();
        int ulY2 = r2.upperLeft.getY();

        int lrX1 = r1.lowerRight.getX();
        int lrY1 = r1.lowerRight.getY();
        int lrX2 = r2.lowerRight.getX();
        int lrY2 = r2.lowerRight.getY();

        if (ulX1 > ulX2 && ulY1 >= ulY2) {
            if (ulY1 - ulY2 > 1) { // must distance at least 2
                Position p2 = randomPOnWall(r2, "up");
                int y1 = RANDOM.nextInt(ulY1 - ulY2 - 1) + ulY2 + 1; // upper: uly1, lower: uly2 + 1
                Position p1temp = randomPOnWall(r1, "left");
                Position p1 = new Position(ulX1, y1);
                if (p1temp.getY() > p1.getY()) {
                    p1 = p1temp;
                }
                Position corridor1 = new Position(p2.getX() + 1, p1.getY());
                addHorizontalHallway(world, corridor1, p1.getX() - p2.getX());
                addCornerDe(world, corridor1, new Orientation(true, false));
                addVerticalHallway(world, p2, p1.getY() - p2.getY());
            } else {
                int y1 = RANDOM.nextInt(ulY1 - lrY1 - 1) + lrY1 + 1;
                int x1 = ulX1;
                Position p1 = new Position(x1, y1);
                Position p2 = randomPOnWall(r2, "right");
                if (p1.getY() > p2.getY()) {
                    p2 = new Position(lrX2, p1.getY());
                } else {
                    p1 = new Position(ulX1, p2.getY());
                }
                addHorizontalHallway(world, p2, p1.getX() - p2.getX() + 1);
            }
        } else if (ulX1 < ulX2 && ulY1 <= ulY2) {
            connect2Rooms(world, r2, r1);
        } else if (ulX1 < ulX2) {
            if (ulY1 - ulY2 > 1) {
                Position p2 = randomPOnWall(r2, "up");
                int y1 = RANDOM.nextInt(ulY1 - ulY2 - 1) + ulY2 + 1;
                Position p1temp = randomPOnWall(r1, "right");
                Position p1 = new Position(lrX1, y1);
                if (p1temp.getY() > p1.getY()) {
                    p1 = p1temp;
                }
                addHorizontalHallway(world, p1, p2.getX() - p1.getX());
                Position corridor1 = new Position(p2.getX() - 1, p1.getY());
                addCornerDe(world, corridor1, new Orientation(true, true));
                addVerticalHallway(world, p2, p1.getY() - p2.getY());
            } else {
                int y1 = RANDOM.nextInt(ulY1 - lrY1 - 2) + lrY1 + 1;
                int x1 = lrX1;
                Position p1 = new Position(x1, y1);
                Position p2 = randomPOnWall(r2, "left");
                if (p1.getY() > p2.getY()) {
                    p2 = new Position(ulX1, p1.getY());
                } else {
                    p1 = new Position(lrX1, p2.getY());
                }
                addHorizontalHallway(world, p1, p2.getX() - p1.getX() + 1);
            }
        } else if (ulX1 > ulX2) {
            connect2Rooms(world, r2, r1);
        } else {
            if (ulY1 >= ulY2) {
                Position p1 = randomPOnWall(r1, "down");
                Position p2 = randomPOnWall(r2, "up");
                if (p1.getX() <= p2.getX()) {
                    p2 = new Position(p1.getX(), ulY2);
                } else {
                    p1 = new Position(p2.getX(), lrY1);
                }
                addVerticalHallway(world, p2, p1.getY() - p2.getY() + 1);
            } else {
                connect2Rooms(world, r2, r1);
            }
        }
    }

    private void addHorizontalHallway(TETile[][] world, Position p, int length) {
        addRow(world, p, length, Tileset.FLOOR);
        for (int i = 0; i < length; i += 1) {
            TETile up = world[p.getX() + i][p.getY() + 1];
            TETile down = world[p.getX() + i][p.getY() - 1];
            if (up.equals(Tileset.NOTHING)) {
                world[p.getX() + i][p.getY() + 1] = Tileset.WALL;
            }
            if (down.equals(Tileset.NOTHING)) {
                world[p.getX() + i][p.getY() - 1] = Tileset.WALL;
            }
        }
    }

    private void addVerticalHallway(TETile[][] world, Position p, int length) {
        addColumn(world, p, length, Tileset.FLOOR);
        for (int i = 0; i < length; i += 1) {
            TETile left = world[p.getX() - 1][p.getY() + i];
            TETile right = world[p.getX() + 1][p.getY() + i];
            if (left.equals(Tileset.NOTHING)) {
                world[p.getX() - 1][p.getY() + i] = Tileset.WALL;
            }
            if (right.equals(Tileset.NOTHING)) {
                world[p.getX() + 1][p.getY() + i] = Tileset.WALL;
            }
        }
    }

    private Position randomPosition(int xUpperBound, int yLowerBound) {
        //x upper: width - 5; x lower: 2; y.upper: height - 2； y.lower: 5
        Position p = new Position(RANDOM.nextInt(xUpperBound),
                RANDOM.nextInt(HEIGHT - yLowerBound) + yLowerBound);
        return p;
    }

    private Room addRoom(TETile[][] world, Position p, int width, int height) {
        Room r = new Room(p, new Position(p.getX() + width - 1, p.getY() - height + 1), false);
        addRow(world, p, width, Tileset.WALL);
        for (int i = 1; i < height; i += 1) {
            world[p.getX()][p.getY() - i] = Tileset.WALL;
            int y = p.getY() - i;
            int x = p.getX() + 1;
            Position newRow = new Position(x, y);
            addRow(world, newRow, width - 2, Tileset.FLOOR);
            world[p.getX() + width - 1][p.getY() - i] = Tileset.WALL;
        }
        Position endRow = new Position(p.getX(), p.getY() - height + 1);
        addRow(world, endRow, width, Tileset.WALL);
        return r;
    }

    private void addRow(TETile[][] world, Position p, int width, TETile t) {
        for (int i = 0; i < width; i += 1) {
            int x = p.getX() + i;
            int y = p.getY();
//            world[x][y] = TETile.colorVariant(t, 32, 32, 32, RANDOM);
            world[x][y] = t;
        }
    }

    private void addColumn(TETile[][] world, Position p, int height, TETile t) {
        for (int i = 0; i < height; i += 1) {
            int x = p.getX();
            int y = p.getY() + i;
//            world[x][y] = TETile.colorVariant(t, 32, 32, 32, RANDOM);
            world[x][y] = t;
        }
    }

    private Position randomPOnWall(Room r, String direction) {
        int x = 0;
        int y = 0;
        if (direction.equals("up")) { //x upper: lr.x; lower: ul.x + 1;
            x = RANDOM.nextInt(r.lowerRight.getX() - r.upperLeft.getX() - 1)
                    + r.upperLeft.getX() + 1;
            y = r.upperLeft.getY();
        } else if (direction.equals("down")) {
            x = RANDOM.nextInt(r.lowerRight.getX() - r.upperLeft.getX() - 1)
                    + r.upperLeft.getX() + 1;
            y = r.lowerRight.getY();
        } else if (direction.equals("left")) { //y upper: ul.y; y lower: lr + 1;
            y = RANDOM.nextInt(r.upperLeft.getY() - r.lowerRight.getY() - 1)
                    + r.lowerRight.getY() + 1;
            x = r.upperLeft.getX();
        } else {
            y = RANDOM.nextInt(r.upperLeft.getY() - r.lowerRight.getY() - 1)
                    + r.lowerRight.getY() + 1;
            x = r.lowerRight.getX();
        }
        return new Position(x, y);
    }

    private Corner addCornerDe(TETile[][] world, Position p, Orientation o) { //decrease
        Position newp;
        if (o.horizontal) {
            if (o.increase) {
                newp = new Position(p.getX() + 1, p.getY());
                world[newp.getX()][newp.getY()]
                        = TETile.colorVariant(Tileset.FLOOR, 32, 32, 32, RANDOM);
                world[newp.getX() + 1][newp.getY()]
                        = TETile.colorVariant(Tileset.WALL, 32, 32, 32, RANDOM);
                addRow(world, new Position(newp.getX(), newp.getY() + 1), 2, Tileset.WALL);
                newp = new Position(newp.getX(), newp.getY() - 1);
                Orientation newo = new Orientation(false, false);
                return new Corner(newp, newo);
            } else {
                newp = new Position(p.getX() - 1, p.getY());
                world[newp.getX()][newp.getY()]
                        = TETile.colorVariant(Tileset.FLOOR, 32, 32, 32, RANDOM);
                world[newp.getX() - 1][newp.getY()]
                        = TETile.colorVariant(Tileset.WALL, 32, 32, 32, RANDOM);
                addRow(world, new Position(newp.getX() - 1, newp.getY() + 1), 2, Tileset.WALL);
                newp = new Position(newp.getX(), newp.getY() - 1);
                Orientation newo = new Orientation(false, false);
                return new Corner(newp, newo);
            }
        } else {
            if (o.increase) {
                newp = new Position(p.getX(), p.getY() + 1);
                world[newp.getX()][newp.getY()]
                        = TETile.colorVariant(Tileset.FLOOR, 32, 32, 32, RANDOM);
                world[newp.getX()][newp.getY() + 1]
                        = TETile.colorVariant(Tileset.WALL, 32, 32, 32, RANDOM);
                addColumn(world, new Position(newp.getX() + 1, newp.getY()), 2, Tileset.WALL);
                newp = new Position(newp.getX() - 1, newp.getY());
                Orientation newo = new Orientation(true, false);
                return new Corner(newp, newo);
            } else {
                newp = new Position(p.getX(), p.getY() - 1);
                world[newp.getX()][newp.getY()]
                        = TETile.colorVariant(Tileset.FLOOR, 32, 32, 32, RANDOM);
                world[newp.getX()][newp.getY() - 1]
                        = TETile.colorVariant(Tileset.WALL, 32, 32, 32, RANDOM);
                addColumn(world, new Position(newp.getX() + 1, newp.getY() - 1), 2, Tileset.WALL);
                newp = new Position(newp.getX() - 1, newp.getY());
                Orientation newo = new Orientation(true, false);
                return new Corner(newp, newo);
            }
        }
    }


    private boolean overlap(TETile[][] world, Position upperLeft, int width, int height) {
        for (int i = 0; i < height; i += 1) {
            Position startP = new Position(upperLeft.getX(), upperLeft.getY() - i);
            if (checkRow(world, startP, width)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRow(TETile[][] world, Position p, int length) {
        for (int i = 0; i < length; i += 1) {
            int x = p.getX() + i;
            int y = p.getY();
            if (!world[x][y].equals(Tileset.NOTHING)) {
                return true;
            }
        }
        return false;
    }

}
