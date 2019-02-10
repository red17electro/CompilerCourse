class OverrideTest {
	public static void main(String[] a) {
	}
}

class A {
	A m(boolean x) {
		return new A();
	}
}

class B {
	A m(int x) {
		return new A();
	}
}
