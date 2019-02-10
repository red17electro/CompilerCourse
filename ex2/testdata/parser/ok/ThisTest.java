class ThisTest {
	public static void main(String[] a) {
		A x;
		x = new A();
		x = x.m();
	}
}

class A {
	A m() {
		return this;
	}
}
