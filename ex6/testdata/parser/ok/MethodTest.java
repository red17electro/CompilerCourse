class MethodTest {
	public static void main(String[] a) {
		A x;
		int y;
		x = new A();
		y = x.m();
	}
}

class A {
	int m() {
		return 0;
	}

}
