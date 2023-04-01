import matplotlib.pyplot as plt


class Point:

    def __init__(self, x: float, y: float) -> None:
        """Creates a point with x, y coordinates."""
        self.x = x
        self.y = y


class Edge:

    def __init__(self, point1: Point, point2: Point) -> None:
        """Creates an edge oriented from y min to y max."""

        if (point1.y < point2.y):
            self.start = point1
            self.end = point2
        else:
            self.start = point2
            self.end = point1

        slope = (self.end.y - self.start.y) / (self.end.x - self.start.x)
        self.inverseSlope = 1 / slope


class Polygon:

    def __init__(self, vertices: list[Point]) -> None:
        """Creates a polygon using a list of points."""

        if len(vertices) < 3:
            raise ValueError("Cannot create polygon with less than 3 vertices")

        self.vertices = vertices

    def center(self):
        """Centers polygon using it's centroid."""

        xSum = 0
        ySum = 0

        for vertex in self.vertices:
            xSum += vertex.x
            ySum += vertex.y

        centroid = Point(xSum/len(self.vertices), ySum/len(self.vertices))

        # Move polygon vertices
        for i, vertex in enumerate(self.vertices):
            self.vertices[i] = Point(
                vertex.x - centroid.x, vertex.y - centroid.y)

    def getMBR(self):
        """Returns polygon's Minimum Bounding Rectangle (MBR)."""

        xMin = self.vertices[0].x
        xMax = self.vertices[0].x
        yMin = self.vertices[0].y
        yMax = self.vertices[0].y

        for vertex in self.vertices:
            if vertex.x < xMin:
                xMin = vertex.x
            if vertex.y < yMin:
                yMin = vertex.y
            if vertex.x > xMax:
                xMax = vertex.x
            if vertex.y > yMax:
                yMax = vertex.y

        return MBR(Point(xMin, yMin), Point(xMax, yMax))

    def getCoordinates(self):
        """Returns a list with the coordinates of polygon's vertices."""
        return [(vertex.x, vertex.y) for vertex in self.vertices]

    def plot(self):
        """Plots polygon."""
        coords = self.getCoordinates()
        coords.append(coords[0])
        xs, ys = zip(*coords)
        plt.plot(xs, ys)


class MBR():

    def __init__(self, minPoint: Point, maxPoint: Point) -> None:
        """Creates a Minimum Bounding Rectangle (MBR)."""
        self.minPoint = minPoint
        self.maxPoint = maxPoint

    def getCoordinates(self):
        """Returns a list with the coordinates of MBR's corners."""
        return [
            (self.minPoint.x, self.minPoint.y),
            (self.minPoint.x, self.maxPoint.y),
            (self.maxPoint.x, self.maxPoint.y),
            (self.maxPoint.x, self.minPoint.y)]

    def plot(self):
        """Plots MBR."""
        coords = self.getCoordinates()
        coords.append(coords[0])
        xs, ys = zip(*coords)
        plt.plot(xs, ys)


class Grid:

    def __init__(self, mbr: MBR, xSize: int, ySize: int) -> None:

        self.tileHeight = (mbr.maxPoint.y - mbr.minPoint.y) / ySize
        self.tileWidth = (mbr.maxPoint.x - mbr.minPoint.x) / xSize
        self.scanlines = []
        self.yBoundaries = []
        self.xBoundaries = []

        halfHeight = self.tileHeight / 2
        for i in range(ySize):
            self.scanlines.append(i*self.tileHeight +
                                  halfHeight + mbr.minPoint.y)
            self.yBoundaries.append(i*self.tileHeight + mbr.minPoint.y)
        self.yBoundaries.append(mbr.maxPoint.y)

        for i in range(xSize+1):
            self.xBoundaries.append(i*self.tileWidth + mbr.minPoint.x)

    def plot(self):
        """Plots rasterization grid."""
        fig, ax = plt.subplots()
        ax.set_xticks(self.xBoundaries)
        ax.set_yticks(self.yBoundaries)
        plt.grid()

