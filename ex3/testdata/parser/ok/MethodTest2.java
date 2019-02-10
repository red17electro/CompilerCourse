class MethodTest {
	public static void main(String[] a) {
		A x;
		boolean b;
		x = new A();
		b = x.m();
	}
}

class A {
	boolean m() {
		return true;
	}

}
