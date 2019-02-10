class addition {
	public static void main(String[] args) {
		int b;
		arrayCarrier a;
		a = new arrayCarrier();
		b = a.GetP()[0];
	}
}

class arrayCarrier {
	int[] p;

	int[] GetP() {
		return p;
	}
}
