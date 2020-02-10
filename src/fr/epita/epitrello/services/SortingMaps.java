package fr.epita.epitrello.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SortingMaps {
	
	/** 
	 * Sorting HashMap DESC by Value: 
	 * this function takes a hashmap as a parameter
	 * a list is created with the values from the Map
	 * then, it calls Collections.sort by taking the list
	 * it then sorts the map by comparing a pair of values with a specific key in a descending
	 * then the result is being returned in a new map by adding each value in it.
	 */
	public static Map<String, Integer> sortByValueDesc(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		Map<String, Integer> result = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
	
	
	/** 
	 * Sorting HashMap ASC by Value 
	 * this function behaves exactly like the previous function
	 * the only difference is that in the compare function, the order of the values in the parameters are inverted.
	 */
	public static Map<String, Integer> sortByValueAsc(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		Map<String, Integer> result = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

}
