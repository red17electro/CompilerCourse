class Main {
	public static void main(String[] args) {
		myClass a;
		boolean foolean;
		a = new myClass();
		foolean = a.SetVal(111);
		System.out.println(a.GetVal());
	}
}

class myClass {
	int a;
	int b;

	boolean SetVal(int arrg) {
		a = arrg;
		return true;
	}

	int GetVal() {
		return a;
	}
}
