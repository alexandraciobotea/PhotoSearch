package ro.upt.cs.photosearch;



/**
 * A simple running average filter
 */
public class RunningAverageFilter {
    private float[] values;
    private int index;
    private int memorySize;
    private double sumValue = 0;

    /**
     * Create a new instance with the given memory size
     *
     * @param memorySize - The length (memory) of the filter
     */
    public RunningAverageFilter(int memorySize) {
        if (memorySize <= 0) {
            throw new InvalidFilterSizeException();
        }
        this.memorySize = memorySize;
        values = new float[memorySize];
        index = 0;
    }

    /**
     * Add a value
     */
    public void addValue(float value) {
        sumValue -= values[index];
        values[index] = value;
        sumValue += value;
        index = (index + 1) % memorySize;
    }

    /**
     * Get the running average value
     */
    public double getAvgValue() {
        return (sumValue / memorySize);
    }

    public static class InvalidFilterSizeException extends RuntimeException {
    }
}
