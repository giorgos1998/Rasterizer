import csv

import matplotlib.pyplot as plt

from containers import Grid, Point, Polygon

SAMPLE_CSV = 'T1NA_fixed.csv'


def readPolygon(id: int, csvPath: str):
    """Read polygon from CSV and parse it."""

    polyFile = open(csvPath)
    polyReader = csv.reader(polyFile)

    # Skip id lines in polygons csv
    for _ in range(id):
        next(polyReader)
    polyID, *polyPoints = next(polyReader)

    polyFile.close()

    # Parse points
    parsedPoints = [Point(eval(x[0]), eval(x[1])) for x in
                    [point.split() for point in polyPoints]]

    # print('ID:', polyID, '\nPoints:', parsedPoints)

    return Polygon(parsedPoints)


polygon = readPolygon(58, SAMPLE_CSV)

polygon.center()
mbr = polygon.getMBR()
grid = Grid(mbr, 50, 50)

grid.plot()
polygon.plot()
mbr.plot()

plt.show()