class Wurst {
	public static void main(String[] msdf) {
		B b;
		A a;
		int u;
		boolean ub;
		b = new C();
		a = b;
		ub = a.init(20);
		System.out.println(b.size());
		if (a.Verwursten())
			System.out.println(1);
		else
			System.out.println(0);
		a = new B();
		if (a.Verwursten())
			u = 7;
		else
			u = 100;
		System.out.println(u);
		System.out.println(b.get(5));
		System.out.println(b.get(-1));
	}
}

class A {
	int a;
	int[] b;

	boolean init(int x) {
		b = new int[x];
		return true;
	}

	int size() {
		return b.length;
	}

	boolean Verwursten() {
		int z;
		z = 0;
		return z < -1;
	}
}

class B extends A {
	boolean init(int x) {
		b = new int[x * 2];
		return false;
	}

	int get(int index) {
		return b[index];
	}
}

class C extends B {
	boolean Verwursten() {
		int z;
		z = 1;
		while (z < b.length) {
			b[z] = b[z - 1] * 2 + 1;
			z = z + 1;
		}
		return true;
	}
}

class D extends A {
}
