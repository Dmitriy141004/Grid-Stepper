package bin.util;

/**
 * Utility-Class with some math operations, which aren't in class {@link Math}.
 *
 */
public class ExtendedMath {
    /**
     * Maps value which is in first range to second range.
     * <center><h2 style="font-family: 'DejaVu Sans'">Table of examples</h2></center>
     * <table style="border-collapse: collapse; color: black">
     *     <tr style="background-color: #4D7A97; color: white; font-family: 'DejaVu Sans';">
     *         <th style="padding: 20px; border-left: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
     *             Value</th>
     *         <th style="padding: 20px; border-right: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
     *             Min in first range</th>
 *             <th style="padding: 20px; border-left: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
     *             Max in first range</th>
     *         <th style="padding: 20px; border-right: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
     *             Min in second range</th>
     *         <th style="padding: 20px; border-right: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
     *             Max in second range</th>
     *         <th style="padding: 20px; border-right: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
     *             Result</th>
     *     </tr>
     *     <tr style="background-color: rgb(230,230,230);">
     *         <td style="padding: 20px; border: 1px solid #BBB;">22.5</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">0.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">45.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">0.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">100.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">50.0</td>
     *     </tr>
     *     <tr style="background-color: rgb(230,230,230);">
     *         <td style="padding: 20px; border: 1px solid #BBB;">1.25</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">1.25</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">1.85</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">1.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">2.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">1.0</td>
     *     </tr>
     *     <tr style="background-color: rgb(230,230,230);">
     *         <td style="padding: 20px; border: 1px solid #BBB;">4.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">2.87</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">9.43</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">0.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">20.0</td>
     *         <td style="padding: 20px; border: 1px solid #BBB;">3.445121951219512</td>
     *     </tr>
     * </table>
     *
     * <p>Mapping formula:
     * <pre><code>
     * (value - minRange1) / (maxRange1 - minRange1) * (maxRange2 - minRange2) + minRange2
     * </code></pre></p>
     *
     * @param value value to map
     * @param minRange1 the lowest value of first range
     * @param maxRange1 the highest value of first range
     * @param minRange2 the lowest value of second range
     * @param maxRange2 the highest value of second range
     * @return mapped value
     */
    public static double map(double value, double minRange1, double maxRange1, double minRange2, double maxRange2) {
        return (value - minRange1) / (maxRange1 - minRange1) * (maxRange2 - minRange2) + minRange2;
    }
}
