class Main {
	public static void main(String[] args) {
		myClass a;
		boolean foolean;
		a = new myClass();
		foolean = a.SetA(111);
		foolean = a.SetB(111);
		if (a.AIsB()) {
			System.out.println(112);
		} else {
			System.out.println(22);
		}
	}
}

class myClass {
	int a;
	int b;

	boolean SetA(int arrg) {
		a = arrg;
		return true;
	}

	boolean SetB(int bee) {
		b = bee;
		return true;
	}

	boolean AIsB() {
		return (new Equalizer()).isEqual(a, b)
				&& (new Equalizer()).isEqualAlt(a, b);
	}
}

class Equalizer {
	boolean isEqual(int x, int y) {
		return (x < y + 1) && (y < x + 1);
	}

	boolean isEqualAlt(int x, int y) {
		return !(x < y) && !(y < x);
	}

}
