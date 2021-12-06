package burl.es.orderbook.spring;

import java.util.ArrayList;
import java.util.Iterator;

public class ConcurrentModification {

	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<>();

		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		ArrayList<Integer> list2 = new ArrayList<>();

		list2.add(1);
		list2.add(2);
		list2.add(3);
		list2.add(4);
		list2.add(5);

		p1: for(Integer value : list){
			p2: for(Integer v2 : list2){
				System.out.println("List Values:" + value+" - "+v2);
				if (value.equals(3)) {
					list.remove(value);
					continue;
				}
			}
		}
//		Iterator<Integer> it = list.iterator();
//		while (it.hasNext()) {
//			Integer value = it.next();
//			System.out.println("List Value:" + value);
//			if (value.equals(3)) {
//				list.remove(value);
//				continue;
//			}
//		}

	}

}