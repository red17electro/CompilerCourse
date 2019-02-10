class OverrideTest {
	public static void main(String[] a) {
		// A a;
		// a = new B();
		// System.out.println(a.m(5));
		System.out.println((new A()).foo());
	}
}

class A {

	int foo() {
		A a;
		int i;
		i = 3;
		a = new B();
		if (i < 5) {
			System.out.println(a.m(50));
			i = 10;
		} else {
			System.out.println(a.m(40));
			i = 12;
		}
		while (i < 100) {
			System.out.println(i);
			i = i + 3;
		}
		return 11111;
	}

	int m(int x) {
		return x + 1;
	}
}

class B extends A {
	int m(int x) {
		return x + 2;
	}
}
