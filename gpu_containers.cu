#include <stdio.h>
#include <assert.h>
#include "gpu_containers.h"

// Creates a new (0,0) point.
__host__ __device__ GPUPoint::GPUPoint()
{
    this->x = 0;
    this->y = 0;
}

/**
 * @brief Creates a new point with given coordinates.
 *
 * @param x The X coordinate.
 * @param y The Y coordinate.
 */
__host__ __device__ GPUPoint::GPUPoint(float x, float y)
{
    this->x = x;
    this->y = y;
}

// Destructor
__host__ __device__ GPUPoint::~GPUPoint()
{
    // printf("Deleting point\n");
}

// Prints the coordinates of the point.
__host__ __device__ void GPUPoint::print()
{
    printf("(%f, %f)\n", x, y);
}

/**
 * @brief Creates a new Polygon that can be used from host and device.
 *
 * @param size The number of points (vertices) the polygon has.
 * @param points The array of points.
 */
__host__ __device__ GPUPolygon::GPUPolygon(int size, GPUPoint points[])
{
    this->size = size;
    this->points = points;
}

// Copy constructor
__host__ __device__ GPUPolygon::GPUPolygon(GPUPolygon &that)
{
    // Using assert to work both on host and device.
    // Stop execution if the polygons don't have the same size.
    assert(this->size == that.size);

    for (int i = 0; i < size; i++)
    {
        this->points[i] = that.points[i];
    }
}

// Copy assignment operator
__host__ __device__ GPUPolygon &GPUPolygon::operator=(const GPUPolygon &that)
{
    if (this != &that)
    {
        // Using assert to work both on host and device.
        // Stop execution if the polygons don't have the same size.
        assert(this->size == that.size);

        for (int i = 0; i < size; i++)
        {
            this->points[i] = that.points[i];
        }
    }
    return *this;
}

// Destructor
__host__ __device__ GPUPolygon::~GPUPolygon()
{
    // printf("Deleting polygon\n");
}

// Prints the polygon points.
__host__ __device__ void GPUPolygon::print()
{
    printf("Polygon:\n");
    for (int i = 0; i < size; i++)
    {
        points[i].print();
    }
}