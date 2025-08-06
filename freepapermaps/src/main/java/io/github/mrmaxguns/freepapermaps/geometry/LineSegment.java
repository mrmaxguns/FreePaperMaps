package io.github.mrmaxguns.freepapermaps.geometry;


public record LineSegment(Coordinate edge1, Coordinate edge2) {
    // Based on https://stackoverflow.com/a/3842240/13501679 and https://www.geeksforgeeks
    // .org/dsa/check-if-two-given-line-segments-intersect/
    public boolean intersects(LineSegment other) {
        Orientation o1 = getOrientation(edge1, edge2, other.edge1);
        Orientation o2 = getOrientation(edge1, edge2, other.edge2);
        Orientation o3 = getOrientation(other.edge1, other.edge2, edge1);
        Orientation o4 = getOrientation(other.edge1, other.edge2, edge2);

        if (o1 != o2 && o3 != o4) {
            return false;
        }

        // Special cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == Orientation.Collinear && onSegment(edge1, other.edge1, edge2)) return true;

        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
        if (o2 == Orientation.Collinear && onSegment(edge1, other.edge2, edge2)) return true;

        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
        if (o3 == Orientation.Collinear && onSegment(other.edge1, edge1, other.edge2)) return true;

        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
        //noinspection RedundantIfStatement
        if (o4 == Orientation.Collinear && onSegment(other.edge1, edge2, other.edge2)) return true;

        return false;
    }

    // Returns true if q is on the segment between p and r.
    private boolean onSegment(Coordinate p, Coordinate q, Coordinate r) {
        return (q.getX() <= Math.max(p.getX(), r.getX()) && q.getX() >= Math.min(p.getX(), r.getX()) &&
                q.getY() <= Math.max(p.getY(), r.getY()) && q.getY() >= Math.min(p.getY(), r.getY()));
    }

    private Orientation getOrientation(Coordinate p, Coordinate q, Coordinate r) {
        double t1 = (q.getY() - p.getY()) * (r.getX() - q.getX());
        double t2 = (q.getX() - p.getX()) * (r.getY() - q.getY());
        double determinant = t1 - t2;

        // Try to compare within a tolerance to avoid direct equality checking with doubles
        final double EPS = 1e-12;
        double scale = Math.max(1.0, Math.max(Math.abs(t1), Math.abs(t2)));
        double tol = EPS * scale;

        if (Math.abs(determinant) <= tol) {
            return Orientation.Collinear;
        }
        return determinant > 0 ? Orientation.Clockwise : Orientation.Counterclockwise;
    }

    private enum Orientation {Collinear, Clockwise, Counterclockwise}
}
