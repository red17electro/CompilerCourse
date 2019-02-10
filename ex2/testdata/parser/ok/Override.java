class Override {
	public static void main(String[] msdf) {
		Base a;
		a = new Random();
		System.out.println(a.function());
		System.out.println(a.init());
		System.out.println(a.function());
	}
}

class Base {
	int x;
	int y;

	int init() {
		x = 1;
		y = 5;
		return x;
	}

	int function() {
		return 8888;
	}
}

class Random extends Base {
	int x;

	int function() {
		int z;
		z = x + y;
		return z;
	}
}
