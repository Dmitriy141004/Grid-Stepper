package util.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility-class with functions for {@link java.util.Map maps/dictionaries}.
 */
public class MapUtils {
    /**
     * Makes {@link HashMap} from 2D array.
     *
     * @param array source 2D array, where each "column" is pair, and each pair is 2-item array.
     * @param <K>   type of keys of new {@link HashMap}.
     * @param <V>   type of values of new {@link HashMap}
     * @return constructed {@link HashMap}.
     * @throws RuntimeException if sub array doesn't contain enough elements to make pair ({@code subArray.length != 2}).
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapFrom2DArray(Object[][] array) {
        Map<K, V> out = new HashMap<>(array.length);

        for (Object[] subArray : array) {
            if (subArray.length != 2)
                throw new RuntimeException("Sub array " + Arrays.toString(subArray) + " doesn\'t " +
                        "contain enough elements to make key-value pair.");

            out.put((K) subArray[0], (V) subArray[1]);
        }

        return out;
    }
}
