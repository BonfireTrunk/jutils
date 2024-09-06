package today.bonfire.jutils.constants;


/**
 * Defines constants for accessing items in an ordered collection.
 *
 * <p>This class provides a set of integer constants representing
 * the indices of the first ten items in a zero-indexed collection. These constants can be used to improve code
 * readability when accessing specific positions in arrays, lists, or other indexed data structures.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * List<String> items = Arrays.asList("A", "B", "C", "D", "E");
 * String secondItem = items.get(ArrayItem.SECOND);
 * String fourthItem = items.get(ArrayItem.FOURTH);
 * </pre>
 */
public class ArrayItem {

  public static final int FIRST   = 0;
  public static final int SECOND  = 1;
  public static final int THIRD   = 2;
  public static final int FOURTH  = 3;
  public static final int FIFTH   = 4;
  public static final int SIXTH   = 5;
  public static final int SEVENTH = 6;
  public static final int EIGHTH  = 7;
  public static final int NINTH   = 8;
  public static final int TENTH   = 9;

  // Private constructor to prevent instantiation
  private ArrayItem() {
    throw new AssertionError("ArrayItem class should not be instantiated.");
  }
}
