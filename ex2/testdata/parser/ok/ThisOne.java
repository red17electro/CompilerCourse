class ThisOne {

	public static void main(String[] args) {
		System.out.println(new A().bla());
	}

}

class A {
	int bla() {
		A a;
		a = this.blubb(new B());
		return a.foo();
	}

	A blubb(A c) {
		return c;
	}

	int foo() {
		return 42;
	}
}

class B extends A {
	int foo() {
		return 99;
	}
}

class C extends B {

}