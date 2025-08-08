package io.github.mrmaxguns.freepapermaps.geometry;


import io.github.mrmaxguns.freepapermaps.projections.RawCoordinate;


public record LineSegment(Coordinate<?> edge1, Coordinate<?> edge2) {
    // Based on https://stackoverflow.com/a/3842240/13501679 and
    // https://www.geeksforgeeks.org/dsa/check-if-two-given-line-segments-intersect/
    public boolean intersects(LineSegment other) {
        Orientation o1 = getOrientation(edge1, edge2, other.edge1);
        Orientation o2 = getOrientation(edge1, edge2, other.edge2);
        Orientation o3 = getOrientation(other.edge1, other.edge2, edge1);
        Orientation o4 = getOrientation(other.edge1, other.edge2, edge2);

        if (o1 != o2 && o3 != o4) {
            // Calculate intersection point, since we don't count simply sharing the same node as an intersection.
            Coordinate<?> intersection = computeIntersectionPoint(this.edge1, this.edge2, other.edge1, other.edge2);

            if (intersection == null) {
                // The intersection should only be null if the lines are parallel, in which case the original condition
                // of this if-statement should not have been triggered.
                throw new RuntimeException("Failed to determine whether line segments intersect.");
            }

            // If intersection is exactly an endpoint, ignore it.
            return !intersection.equals(edge1) && !intersection.equals(edge2) && !intersection.equals(other.edge1) &&
                   !intersection.equals(other.edge2);  // touching at endpoint only
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
    private boolean onSegment(Coordinate<?> p, Coordinate<?> q, Coordinate<?> r) {
        return (q.getX() <= Math.max(p.getX(), r.getX()) && q.getX() >= Math.min(p.getX(), r.getX()) &&
                q.getY() <= Math.max(p.getY(), r.getY()) && q.getY() >= Math.min(p.getY(), r.getY()));
    }

    private Orientation getOrientation(Coordinate<?> p, Coordinate<?> q, Coordinate<?> r) {
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

    public boolean isDegenerate() {
        return edge1.equals(edge2);
    }

    private enum Orientation {Collinear, Clockwise, Counterclockwise}

    private Coordinate<?> computeIntersectionPoint(Coordinate<?> p1, Coordinate<?> q1, Coordinate<?> p2,
                                                   Coordinate<?> q2) {
        double A1 = q1.getY() - p1.getY();
        double B1 = p1.getX() - q1.getX();
        double C1 = A1 * p1.getX() + B1 * p1.getY();

        double A2 = q2.getY() - p2.getY();
        double B2 = p2.getX() - q2.getX();
        double C2 = A2 * p2.getX() + B2 * p2.getY();

        double det = A1 * B2 - A2 * B1;

        if (det == 0) {
            // Lines are parallel
            return null;
        } else {
            double x = (B2 * C1 - B1 * C2) / det;
            double y = (A1 * C2 - A2 * C1) / det;
            return new RawCoordinate(x, y);
        }
    }
}
