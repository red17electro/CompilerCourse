class Scopes {

	public static void main(String[] args) {
		A a;
		a = new A();
		System.out.println(a.change());
		System.out.println(a.marp());
		System.out.println(a.morp());
		System.out.println(new A().muh().pong());
	}

}

class A {

	int i;

	int change() {
		B a;
		a = new B();
		i = 5;
		return i;
	}

	int marp() {
		int i;
		A a;
		a = new A();
		i = 7;
		System.out.println(a.morp());
		return i;
	}

	int morp() {
		B a;
		a = new B();
		System.out.println(a.peng());
		return i;
	}

	B muh() {
		B a;
		a = new B();
		System.out.println(a.peng());
		return a;
	}

}

class B {

	A a;

	int peng() {
		a = new A();
		return 99;
	}

	int pong() {
		B b;
		a = new A();
		return a.muh().peng();
	}
}