class Main {
	public static void main(String[] args) {
		System.out.println(new Eins().GetVal());
		System.out.println(new Zwei().GetVal());
	}
}

class Eins {
	int GetVal() {
		return 1;
	}
}

class Zwei extends Eins {
	int GetVal() {
		return 2;
	}
}
