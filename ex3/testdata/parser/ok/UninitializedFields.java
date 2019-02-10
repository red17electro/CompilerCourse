class Bla {
	public static void main(String[] args) {
		int foo;
		B b;
		b = new B();
		System.out.println(1);
		foo = b.someMethod();
		System.out.println(2);
	}
}

class B {
	int x;
	boolean y;

	int someMethod() {
		if (y) {
			System.out.println(667);
		} else {
			System.out.println(x);
		}
		return 666;
	}
}