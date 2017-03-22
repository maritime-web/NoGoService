/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.nogoservice.algo;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An Algorithm for taking a grid (List of rows) and finds polygons coordinates identifying areas with similar values
 * The are processes the grid one line at a time, and if the current line connects with the previous line it extends the polygon area
 * The algorithm is general, but used to find NoGo areas
 *
 * @author Klaus Groenbaek
 *         Created 13/03/17.
 */
@Slf4j
public class NoGoAlgorithm<Value> {

    private final NoGoMatcher<Value> matcher;
    private final List<List<Value>> rowsOfColumns;
    private final PolygonOptimizer optimizer;

    /**
     * A list of Rows containing a list of columns. This means that the inner list has a constant m (y) value
     * @param rowsOfColumns a grid of data
     * @param matcher a matching algorithm
     * @param optimizer a polygon optimizer
     */
    public NoGoAlgorithm(List<List<Value>> rowsOfColumns, NoGoMatcher<Value> matcher, PolygonOptimizer optimizer) {
        this.rowsOfColumns = rowsOfColumns;
        this.matcher = matcher;
        this.optimizer = optimizer;
        if (rowsOfColumns.isEmpty()) {
            throw new IllegalArgumentException("No rows");
        }
        if (rowsOfColumns.get(0).isEmpty()) {
            throw new IllegalStateException("No Columns");
        }
    }

    /**
     * Breaks the grid into lines, and the joins the lines to form figures
     *
     * @return a list of figures that identifies the coordinates which matched
     */
    public List<Figure> getFigures() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<List<LineSegment>> rows = new ArrayList<>();
            for (int y = 0; y < rowsOfColumns.size(); y++) {
                List<Value> columns = rowsOfColumns.get(y);
                Boolean previousValue = null;
                LineSegment builder = new LineSegment();
                List<LineSegment> linesInRow = new ArrayList<>();
                for (int x = 0; x < columns.size(); x++) {
                    Value value = columns.get(x);
                    boolean noGo = matcher.matches(value);
                    if (previousValue == null) {
                        previousValue = noGo;
                        if (noGo) {
                            builder.start = new Point(x, y);
                        }
                    } else {
                        if (previousValue != noGo) {
                            // value has changed

                            if (!noGo) {
                                builder.end = new Point(x - 1, y);
                                linesInRow.add(builder);
                                builder = new LineSegment();
                            } else {
                                builder.start = new Point(x, y);
                            }
                            previousValue = noGo;
                        }
                    }
                }
                if (builder.start != null && builder.end == null) {
                    builder.end = new Point(columns.size() - 1, y);
                    linesInRow.add(builder);
                }
                rows.add(linesInRow);
            }

            return joinLines(rows);
        } finally {
            long millis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            log.info("Processed " + rowsOfColumns. size() + "x" + rowsOfColumns.get(0).size() + " in " + millis + " ms");
        }
    }

    /**
     * Joins rows of list of line segment. If a line segment from to consecutive rows touch they may be joined into a polygon
     */
    private List<Figure> joinLines(List<List<LineSegment>> rows) {

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        List<LineJoiner> activeJoiners = new ArrayList<>();
        List<LineJoiner> inactiveJoiners = new ArrayList<>();
        for (List<LineSegment> row : rows) {

            for (Iterator<LineJoiner> iterator = activeJoiners.iterator(); iterator.hasNext() && !row.isEmpty(); ) {
                LineJoiner joiner = iterator.next();
                if (!joiner.consumeMatchingLines(row)) {
                    // this joiner did not match anything, and is now inactive.
                    iterator.remove();
                    inactiveJoiners.add(joiner);
                }
            }

            // any lines that were not joined with anything get their own joiner for the next row
            for (LineSegment lineSegment : row) {
                activeJoiners.add(new LineJoiner(lineSegment));
            }
        }

        inactiveJoiners.addAll(activeJoiners);

        return inactiveJoiners.stream().map(LineJoiner::getFigure).collect(Collectors.toList());
    }

    /**
     * Keeps the state of a figure being build. It stores the last left and right position, and records when the left and right sides of the polygon moves
     * In the end the recorded points can be inspected and used to construct a figure
     */
     class LineJoiner {
        List<Point> leftPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();
        private Point lastLeft;
        private Point lastRight;
        private int layers = 1;


        LineJoiner(LineSegment startLine) {
            leftPoints.add(startLine.start);
            rightPoints.add(startLine.end);
            lastLeft = startLine.start;
            lastRight = startLine.end;

        }

        /**
         * Joins lines that matches the previous line. This method removes the joined LineSegment from the row
         * If the joiner does not match anything
         *
         * @param row the row
         * @return true if this consumed anything. If nothing was consumed this joiner may be removed from duty, since it will never match the next layer
         */
        boolean consumeMatchingLines(List<LineSegment> row) {

            LineSegment matching = findMatching(row);
            if (matching != null) {
                layers++;
                row.remove(matching);
            }
            return matching != null;
        }

        private LineSegment findMatching(List<LineSegment> row) {
            for (int i = 0; i < row.size(); i++) {
                LineSegment current = row.get(i);

                if (lastLeft.x > current.end.x || lastRight.x < current.start.x) {
                    // the line starts before this layer ends, or ends before this layer starts
                    continue;
                }

                // notice the offset calculation is different, this is go give consistent semantics
                int leftOffset = lastLeft.x - current.start.x;
                int rightOffset = current.end.x - lastRight.x;

                if (leftOffset == 0) {
                    // same start as previous layer
                    if (rightOffset == 0) {
                        if (current.length() != 0) {
                            lastRight = current.end;
                        }
                        lastLeft = current.start;
                        return current;
                    }
                    if (rightOffset < 0) {
                        // line ends inside
                        if (current.length() != 0) {
                            lastLeft = current.start;
                        }
                        return setNewRight(current);

                    }
                    if (rightOffset > 0) {
                        // line ends outside
                        if (doesNotOverlapWithNext(row, i)) {
                            lastLeft = current.start;
                            return setNewRight(current);
                        }
                    }
                }
                if (leftOffset > 0) {
                    // line starts outside
                    if (rightOffset == 0) {
                        if (current.length() != 0) {
                            lastRight = current.end;
                        }
                        return setNewLeft(current);
                    }
                    if (rightOffset < 0) {
                        // ends inside
                        if (doesNotOverlapWithNext(row, i)) {
                            setNewLeft(current);
                            return setNewRight(current);
                        }
                    }
                    if (rightOffset > 0) {
                        // ends outside
                        setNewLeft(current);
                        return setNewRight(current);
                    }
                }
                if (leftOffset < 0) {
                    // line starts inside
                    if (rightOffset == 0) {
                        if (current.length() != 0) {
                            lastRight = current.end;
                        }
                        return setNewLeft(current);
                    }
                    if (rightOffset < 0) {
                        // ends inside
                        setNewLeft(current);
                        return setNewRight(current);
                    }
                    if (rightOffset > 0) {
                        // ends outside
                        if (doesNotOverlapWithNext(row, i)) {
                            setNewLeft(current);
                            return setNewRight(current);
                        }
                    }
                }
            }
            return null;
        }

        private boolean doesNotOverlapWithNext(List<LineSegment> row, int i) {
            // new line is shorter than top layer, only allow it if does not overlap the next line in the row
            if (i == row.size() - 1) {
                return true;
            }
            if (i + 1 < row.size()) {
                // there is another line in the row, check if it overlaps
                LineSegment next = row.get(i + 1);
                if (next.start.x > lastRight.x) {
                    return true;
                }
            }
            return false;
        }

        private LineSegment setNewRight(LineSegment current) {
            if (lastRight.x != current.end.x) {
                if (!rightPoints.get(rightPoints.size() - 1).equals(lastRight)) {
                    rightPoints.add(lastRight);
                }
            }
            lastRight = current.end;
            rightPoints.add(lastRight);
            return current;
        }

        private LineSegment setNewLeft(LineSegment current) {
            // When we have the same consecutive x value we don't add lastLeft, so when the x value change we need to add it (since it was a corner)
            if (lastLeft.x != current.start.x) {
                if (!leftPoints.get(leftPoints.size() - 1).equals(lastLeft)) {
                    leftPoints.add(lastLeft);
                }
            }
            lastLeft = current.start;
            leftPoints.add(lastLeft);
            return current;
        }

        Figure getFigure() {

            if (layers == 1) {
                if (lastLeft.equals(lastRight)) {
                    return new SinglePoint(lastLeft);
                }
                return new Line(lastLeft, lastRight);
            }

            if (!leftPoints.contains(lastLeft)) {
                // the last left point has to be added
                leftPoints.add(lastLeft);
            }
            if (!rightPoints.contains(lastRight)) {
                rightPoints.add(lastRight);
            }
            return createPolygon();

        }

        private Figure createPolygon() {
            Point start = leftPoints.get(0);
            if (rightPoints.get(0).equals(start)) {
                // we started in a point
                rightPoints.remove(start);
            }

            // special case if there are only two left points, and no right points, we have a vertical line
            if (leftPoints.size() == 2 && rightPoints.size() == 0) {
                return new Line(leftPoints);
            }

            LinkedList<Point> points = new LinkedList<>(leftPoints);
            Collections.reverse(rightPoints);   // we have gone up the left side, now we go down the right side
            points.addAll(rightPoints);
            points.add(start);          // loop back to start WKT style
            return optimizer.optimize(new Polygon(points));
        }
    }

    static class LineSegment {
        Point start;
        Point end;

        int length() {
            return end.x - start.x;
        }

        @Override
        public String toString() {
            return start + "=>" + end;
        }
    }

}
