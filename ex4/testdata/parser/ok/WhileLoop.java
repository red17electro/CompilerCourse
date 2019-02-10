class Main {
	public static void main(String[] args) {
		int i;
		Loud loud;
		loud = new Loud();

		i = 1;
		while (loud.withSideEffect(i, 0) < 5) {
			System.out.println(i);
			i = i + 1;
		}
		System.out.println(100);
	}
}

class Loud {
	int withSideEffect(int x, int printee) {
		System.out.println(printee);
		return x;
	}
}