class Main {
	public static void main(String[] args) {
		myClass a;
		a = new myClass();
		System.out.println(a.tee());
	}
}

class myClass {
	int a;
	int b;

	int tee() {
		int a;
		a = 14;
		return a;
	}
}
