/**
 * Various ways of setting fields
 * 
 * @author anton
 *
 */

class Main {
	public static void main(String[] args) {
		A a;
		B b;
		C c;
		int wurst;
		c = new C();

		wurst = c.printAll();
		wurst = c.setAa(1);
		wurst = c.printAll();
		wurst = c.setBb(2);
		wurst = c.printAll();
		wurst = c.setCc(3);
		wurst = c.printAll();
		wurst = c.setAllAscending(4);
		wurst = c.printAll();
	}
}

class A {
	int aa;

	int setAa(int x) {
		aa = x;
		return aa;
	}
}

class B extends A {
	int bb;

	int setBb(int x) {
		bb = x;
		return bb;
	}
}

class C extends B {
	int cc;

	int setCc(int x) {
		cc = x;
		return cc;
	}

	int setAllAscending(int x) {
		aa = x;
		bb = x + 1;
		cc = x + 2;
		return 0;
	}

	int printAll() {
		System.out.println(aa);
		System.out.println(bb);
		System.out.println(cc);
		return 0;
	}
}