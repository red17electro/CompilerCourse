class Inherit {

	public static void main(String[] args) {
		B b;
		C c;
		b = new B();
		c = new C();
		System.out.println(b.change());
		System.out.println(c.change());
		System.out.println(b.bla());
		System.out.println(c.get());
	}
}

class A {
	int i;
	int j;

	int print() {
		return 8;
	}

	int change() {
		i = 7;
		return 0;
	}
}

class B extends A {

	int blubb() {
		return 1;
	}

	int bla() {
		i = i + 1;
		return i;
	}

}

class C {
	int i;

	int change() {
		i = 99;
		return 0;
	}

	int get() {
		return i;
	}
}