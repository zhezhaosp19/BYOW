package byow.Core;
import java.util.List;

public class KDTree implements PointSet {
    private Node root;

    private class Node {
        private Position point;
        private boolean horizontal;
        private Node left;
        private Node right;

        Node(Position p, boolean h) {
            point = p;
            horizontal = h;
        }
    }

    public KDTree(List<Position> points) {
        for (Position p : points) {
            root = add(root, p);
        }
    }

    private Node add(Node node, Position point) {
        if (point == null) {
            throw new IllegalArgumentException();
        }
        if (node == null) {
            return new Node(point, true);
        } else if (point.getX() == node.point.getX() && point.getY() == node.point.getY()) {
            return node;
        } else if (node.left == null && node.right == null) {
            if (node.horizontal) {
                if (node.point.getX() > point.getX()) {
                    node.left = new Node(point, false);
                } else {
                    node.right = new Node(point, false);
                }
            } else {
                if (node.point.getY() > point.getY()) {
                    node.left = new Node(point, true);
                } else {
                    node.right = new Node(point, true);
                }
            }
        } else {
            if (node.horizontal) {
                if (node.point.getX() > point.getX()) {
                    node.left = add(node.left, point);
                    node.left.horizontal = false;
                } else {
                    node.right = add(node.right, point);
                    node.right.horizontal = false;
                }
            } else {
                if (node.point.getY() > point.getY()) {
                    node.left = add(node.left, point);
                    node.left.horizontal = true;
                } else {
                    node.right = add(node.right, point);
                    node.right.horizontal = true;
                }
            }
        }
        return node;
    }

    @Override
    public Position nearest(int x, int y) {
        Position goal = new Position(x, y);
        return nearest(root, goal, root).point;
    }

    private Node nearest(Node node, Position goal, Node best) {
        if (node == null) {
            return best;
        }
        if (Position.distance(node.point, goal) < Position.distance(best.point, goal)) {
            best = node;
        }
        Node good = node.left;
        Node bad = node.right;
        if (!larger(node, goal)) {
            good = node.right;
            bad = node.left;
        }

        best = nearest(good, goal, best);

        double badDistance = Math.pow(node.point.getY() - goal.getY(), 2);
        if (node.horizontal) {
            badDistance = Math.pow(node.point.getX() - goal.getX(), 2);
        }
        if (bad == null || badDistance > Position.distance(best.point, goal)) {
            return best;
        }

        Node nearestBad = nearest(bad, goal, best);
        if (Position.distance(best.point, goal)
                > Position.distance(nearestBad.point, goal)) {
            best = nearestBad;
        }
        return best;
    }
        /*if (bad.horizontal) {
            double badDistance = Math.pow(bad.point.getX() - goal.getX(), 2);
            if (badDistance < Point.distance(best.point, goal)) {
                Node nearestBad = nearest(bad, goal, best);
                if (Point.distance(nearestGood.point, goal) >
                        Point.distance(nearestBad.point, goal)) {
                    return nearestBad;
                } else {
                    return nearestGood;
                }
            } else {
                return nearestGood;
            }
        } else {
            double badDistance = Math.pow(bad.point.getY() - goal.getY(), 2);
            if (badDistance < Point.distance(best.point, goal)) {
                Node nearestBad = nearest(bad, goal, best);
                if (Point.distance(nearestGood.point, goal) >
                        Point.distance(nearestBad.point, goal)) {
                    return nearestBad;
                } else {
                    return nearestGood;
                }
            } else {
                return nearestGood;
            }
        }
    }
        /*Node nearestBad = nearest(bad, goal, best);
        if (bad == null) {
            return nearestGood;
        }
        if (bad.horizontal) {
             double badDistance = Math.pow(bad.point.getX() - goal.getX(), 2);
             if (badDistance < Point.distance(best.point, goal) &&
                     Point.distance(nearestGood.point, goal) >
                     Point.distance(nearestBad.point, goal)) {
                 return nearestBad;
             } else {
                 return nearestGood;
                 }
        } else {
            double badDistance = Math.pow(bad.point.getY() - goal.getY(), 2);
            if (badDistance < Point.distance(best.point, goal) &&
                    Point.distance(nearestGood.point, goal) >
                    Point.distance(nearestBad.point, goal)) {
                return nearestBad;
            } else {
                return nearestGood;
            }
        }
    }*/

    private boolean larger(Node n, Position p) {
        if (n.horizontal) {
            return (n.point.getX() > p.getX());
        } else {
            return (n.point.getY() > p.getY());
        }
    }

    /*nearest(Node n, Point goal, Node best):
If n is null, return best
If n.distance(goal) < best.distance(goal), best = n
If goal < n (according to n’s comparator):
goodSide = n.”left”Child
badSide = n.”right”Child
else:
goodSide = n.”right”Child
badSide = n.”left”Child
best = nearest(goodSide, goal, best)
If bad side could still have something useful
best = nearest(badSide, goal, best)
return best
*/

//    public static void main(String[] args) {
//        Point p1 = new Point(2, 3); // constructs a Point with x = 1.1, y = 2.2
//        Point p2 = new Point(4, 2);
//        Point p3 = new Point(4, 2);
//        Point p4 = new Point(4, 5);
//        Point p5 = new Point(3, 3);
//        Point p6 = new Point(1, 5);
//        Point p7 = new Point(4, 4);
//
//        KDTree kd = new KDTree(List.of(p1, p2, p3, p4, p5, p6, p7));
//    }
}
