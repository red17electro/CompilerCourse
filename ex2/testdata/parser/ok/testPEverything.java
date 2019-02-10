class testPEverything {
	public static void main(String[] a1) {
		T2 a;
		T7 t;
		int[] b;
		t = new T7();
		a = t;
		b = new int[100];
		System.out.println(t.correctOrder());
		System.out.println(a.arrays(b)[5]);
		System.out.println(a.test(b[0], true));
	}
}

class T2 {
	T2 theSecond;
	T3 theThird;
	int superInt;

	int[] arrays(int[] t) {
		int x;
		t[this.test(3, false)] = this.test(3, false);
		x = this.test(t.length, false);
		return new int[t.length];
	}

	int test(int x, boolean y) {
		int z;
		T2 t;
		z = 7;
		if (((3 * (x + 2) * 3) < (1 - 0 * 5 + (-4) * (-z))) && !false) {
		} else {
		}
		t = new T2();
		theSecond = new T3();
		if (x < 5)
			z = this.test(t.test(theSecond.test(10 + (1 - 0 * x + (-4)),
					(((3 * (4 + 2) * 3) < (1 - 0 * 5 + (-4))) && !y)), false),
					true);
		else {
		}
		return z;
	}

	boolean testBoolean() {
		return false;
	}

	int testInt() {
		return 1;
	}
}

class T3 extends T2 {
	int test2() {
		int x;
		if (this.testBoolean()) {
			x = superInt + this.testInt();
		} else {
			x = 18;
			System.out.println(this.testInt());
		}
		return x;
	}
}

class T4 extends T2 {
}

class T5 extends T4 {
}

class T6 extends T5 {
	int myTest() {
		int x;
		if (this.testBoolean()) {
			x = superInt + this.testInt();
		} else {
			System.out.println(this.testInt());
		}
		return 0;
	}

	boolean TestInT6() {
		return true;
	}

	T4 Test() {
		return new T5();
	}
}

class T7 extends T6 {
	/* Override */boolean TestInT6() {
		return true;
	}

	int correctOrder() {
		return this.myTest();
	}
}
