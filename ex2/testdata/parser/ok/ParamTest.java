class ParamTest {
	public static void main(String[] a) {
		A x;
		x = new A();
		x = x.m(0);
	}
}

class A {
	A m(int i) {
		return new A();
	}

}
