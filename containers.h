#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <iomanip>
#include <iostream>
#include <string>
#include <fstream>
#include <ctime>
#include <algorithm>
#include <vector>
#include <math.h>
#include <cmath>
#include <ctime>
#include <map>
#include <unordered_map>


#include <boost/geometry.hpp>
#include <boost/geometry/algorithms/assign.hpp>
#include <boost/foreach.hpp>


typedef boost::geometry::model::d2::point_xy<double> point_xy;
typedef boost::geometry::model::polygon<boost::geometry::model::d2::point_xy<double> > polygon;


using namespace std;

#ifndef STRUCTURE_H
#define STRUCTURE_H

//4 byte
//typedef uint ID;
//typedef uint32_t CONTAINER;
//8 byte
//typedef unsigned long ID;
//typedef uint64_t CONTAINER;
int VAR_TYPE;	//0 = uint, 1 = unsigned long

double e = 1e-08;
//2^16 = 65536
//2^17 = 131072
//2^18 = 262144
//2^19 = 524288
//2^20 = 1048576      (killed, too slow)
//2^31 = 2147483648
//2^32 = 4294967296   (integer overflow, due to mappping maybe)
//uint64_t HILBERT_n = 131072;
#define HILBERT_n 131072

#if HILBERT_n > 65536
	typedef unsigned long ID;
	typedef uint64_t CONTAINER;
#else
	typedef uint ID;
	typedef uint32_t CONTAINER;
#endif

int DECIMAL_POINTS_PRECISION = 6;

double loadingGeometriesTime = 0;

double decompressTime = 0;

unsigned long totalIntervalsALL = 0;
unsigned long totalCellsALL = 0;
unsigned long totalIntervalsSF = 0;
unsigned long totalCellsSF = 0;
unsigned long totalIntervalsF = 0;
unsigned long totalCellsF = 0;
unsigned long refinementCandidatesR = 0;
unsigned long refinementCandidatesS = 0;

//universal coordinates of the 2 datasets
double universalMinX = std::numeric_limits<Coord>::max();
double universalMinY = std::numeric_limits<Coord>::max();
double universalMaxX = -std::numeric_limits<Coord>::max();
double universalMaxY = -std::numeric_limits<Coord>::max();

//if there are at least this many full cells continuously in an interval,
// then it is worth to split it (x*3 / 8 <= 16 -> x = 42.6 cells)
int MIN_CONSECUTIVE_FULL = 43;


#define BYTE_TO_BINARY_PATTERN "%c%c%c%c%c%c%c%c"
#define BYTE_TO_BINARY(byte)  \
  (byte & 0x80 ? '1' : '0'), \
  (byte & 0x40 ? '1' : '0'), \
  (byte & 0x20 ? '1' : '0'), \
  (byte & 0x10 ? '1' : '0'), \
  (byte & 0x08 ? '1' : '0'), \
  (byte & 0x04 ? '1' : '0'), \
  (byte & 0x02 ? '1' : '0'), \
  (byte & 0x01 ? '1' : '0') 


void setIDtype(){
	if(HILBERT_n > 65536){
		//typedef unsigned long ID;
		//typedef uint64_t CONTAINER;
		VAR_TYPE = 1;
		cout << "N = " << log2(HILBERT_n) << ", ID set to ulong" << endl;
	}else{
		//typedef uint ID;
		//typedef uint32_t CONTAINER;
		VAR_TYPE = 0;
		cout << "N = " << log2(HILBERT_n) << ", ID set to uint" << endl;
	}
}

/*
*-------------------------------------------------------
*
*     CLASSES
*       
*
*-------------------------------------------------------
*/



class Point{
public:
	double x, y;
	Point(double x, double y){
		this->x = x;
		this->y = y;
	}
	Point(){}

	bool operator< (const Point &other) const{
        return x < other.x;
    }

    bool operator== (const Point &other) const{
        return (this->x == other.x && this->y == other.y);
	}

	double to_angle(Point &o){   
		return atan2(y - o.y, x - o.x);
	}
};


// A struct that will contain a line segment of a polygon. For plane sweep
struct LineSegment
{
    Point startPoint, endPoint;
    bool firstPoint; // If firstPoint is true, then p1 is the new Point, else its p2.
};

typedef vector<LineSegment>::iterator LineSegmentIterator;

class MBR{
public:
	Point pMin;
	Point pMax;
	MBR(double xS, double yS, double xE, double yE){
		pMin = Point(xS, yS);
		pMax = Point(xE, yE);
	}
	void set(double xS, double yS, double xE, double yE){
		pMin = Point(xS, yS);
		pMax = Point(xE, yE);
	}
	MBR(){};
};


class TempPolygon{
public:
	uint recID;
	vector<Point> vertices;
	double cellX, cellY;
	TempPolygon(){}
	
	TempPolygon(uint &recID){
		this->recID = recID;
	}

	void addPoint(Point &p){
		if(find(vertices.begin(), vertices.end(), p) == vertices.end()){
			vertices.push_back(p);
		}

		//vertices.push_back(p);
	}

};

class Cell{
public:
	int classificationID;
	Point bottomLeft;
	Point topRight;

	Cell(){};

	Cell(double bottomLeftX, double bottomLeftY, double topRightX, double topRightY){
		bottomLeft.x = bottomLeftX;
		bottomLeft.y = bottomLeftY;
		topRight.x = topRightX;
		topRight.y = topRightY;
	}

	Cell(double bottomLeftX, double bottomLeftY, double topRightX, double topRightY, int classificationID){
		bottomLeft.x = bottomLeftX;
		bottomLeft.y = bottomLeftY;
		topRight.x = topRightX;
		topRight.y = topRightY;
		this->classificationID = classificationID;
	}


};

class Interval{
public:
	ID start;
	ID end;
	uint8_t* color;

	Interval(ID &s, ID &e){
		start = s;
		end = e;
	}

	Interval(ID &s, ID &e, uint8_t *c){
		start = s;
		end = e;
		
		color = (uint8_t*) malloc(((e-s+1) + 1) * sizeof(uint8_t));
		memcpy(color, c, (e-s+1) + 1);
	}
	Interval(){}
};


class Polygon{
public:	
	//original mbr
	MBR mbr;
	//---rasterization/intervals---
	//IMPORTANT!!!!: DO NOT ATTEMPT TO REMOVE, FOR SOME FUCKING REASON IT BREAKS CODE TO KEEP THEM
	// LOCALLY IN A FUNCTION
	//only for computation, then deleted
	vector<Cell> rasterizationCells;
	vector<ID> hilbertCellIDs;
	unordered_map<ID, Cell> hilbertCells;

	//for planesweep refinement
	vector<LineSegment> exactGeometry_LS;

	//only these are needed for RI join, the rest are cleared
	uint recID;
	vector<Point> vertices;
	polygon boostPolygon;
	//raster intervals
	uint numBytesALL;
	uint numBytesF;
	uint numBytesSF;

	uint numIntervalsALL;
	uint numIntervalsSF;
	uint numIntervalsF;

	vector<uint8_t> compressedALL;
	vector<uint8_t> compressedSF;
	vector<uint8_t> compressedF;

	uint8_t *compressedALLarray;
	uint8_t *compressedSFarray;
	uint8_t *compressedFarray;

	bool SF = false, F = false;

	Polygon(uint &recID){
		this->recID = recID;
	}

	Polygon(){};

	void addPoint(Point &p){
		vertices.push_back(p);
	}

	void addHilbertCell(Cell &cell, ID &cellID){		
		hilbertCellIDs.push_back(cellID);
		hilbertCells.insert(make_pair(cellID, cell));
	}
};

class Dataset{
public:
	string filename;
	unordered_map<uint,Polygon> polygons;
	
	Point pMin, pMax;
	string letterID;

	//for example T1, T2 etc...
	string argument;

	Dataset(){};

	Dataset(string &filename){
		this->filename = filename;
	}

	Dataset(string letterID){
		this->letterID = letterID;
	}

	Dataset(string argument, string letterID){
		this->argument = argument;
		this->letterID = letterID;
	}

	Polygon* getPolygonByID(uint &recID){
		auto it = polygons.find(recID);
		if(it != polygons.end()){
			return &(it->second);
		}
		return NULL;
	}

};

void printContainer(uint8_t *container, uint &totalBytes){
	cout << "CONTAINER:" << endl;
	for(int i = 0; i<totalBytes; i++){
		printf(""BYTE_TO_BINARY_PATTERN" ",BYTE_TO_BINARY(container[i]));
		if((i+1) % 4 == 0){

			cout << endl;
		}
	}
	cout << endl;
}


//DECLARE GLOBAL GEOMETRY DATASETS
Dataset geometryDatasetA("A");
Dataset geometryDatasetB("B");


#endif