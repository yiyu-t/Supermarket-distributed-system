import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DataComparator implements Comparator<Map<Integer, Integer>> {

  // -1 : o1 < o2
  //0 : o1 == o2
  //+1 : o1 > o2

  @Override
  public int compare(Map<Integer, Integer> o1, Map<Integer, Integer> o2) {
    Integer v1 = null;
    Integer v2 = null;

    for (Map.Entry<Integer, Integer> pair1 : o1.entrySet()) {
      v1 = pair1.getValue();
      System.out.println(v1);
    }

    for (Map.Entry<Integer, Integer> pair2 : o2.entrySet()) {
      v2 = pair2.getValue();
      System.out.println(v2);
    }

    if (v1 != null && v2 != null && (v1.intValue() < v2.intValue())) {
      return -1;
    } else if (v1 != null && v2 != null && (v1.intValue() > v2.intValue())) {
      return 1;
    }
    return 0;
  }

}

//
//0  class Main {
//   public static void main(String[] argv) {
//    Map<Integer, Integer> map1 = new HashMap<>();
//    Map<Integer, Integer> map2 = new HashMap<>();
//    map1.put(4, 2);
//    map2.put(2, 3);
//
//    ArrayList arr = new ArrayList();
//     arr.add(map2);
//    arr.add(map1);
//
//    Collections.sort(arr, new DataComparator());
//     System.out.println(arr);
//
//
//  }
//}
