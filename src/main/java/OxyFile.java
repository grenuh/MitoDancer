import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Typical interface of data file
 */
public interface OxyFile {

    /**
     * Proposed getter of chart
     *
     * @return chart ith experimant data
     */
    XChartPanel<XYChart> getChart();

    /**
     * Getter of numbers actions on experiment
     *
     * @return counts of addition
     */
    int getAdditionSize();

    /**
     * May create new file (or update currrent) with belowed hangesunkn
     *
     * @param removedAddition actions to delete
     * @param newAdd          action to add
     * @throws IOException exception
     */
    void modify(ArrayList<Integer> removedAddition, int newAdd) throws IOException;

    /**
     * Update adjustment with new constant of oxygen
     *
     * @param calibr   points of calibration
     * @param iniArray adjustment data
     */
    void getMagicNumber(ArrayList<Integer> calibr, int[] iniArray);

    /**
     * Getter of path file
     *
     * @return path
     */
    String getPath();

    /**
     * Unified getter of analysis result
     * Structure: raw data, time step, list of actions, path to file
     * For keeping MVC model
     * @return big object
     */
    Object[] getData();
}
