class Simple {
	public static void main(String[] args){
		List l;
		
		l = new List();
		l = l.add(3);
		l = l.add(2);
		l = l.add(1);
		System.out.println(l.size());
	}
}

class List {
	
	int size() {
		return 0;
	}
	
	List add(int x) {
		ConsList res;
		res = new ConsList();
		res.head = x;
		res.tail = this;
		return res;
	}
	
}

class ConsList extends List {
	int head;
	List tail;
	
	int size() {
		return 1 + tail.size();
	}
	
}