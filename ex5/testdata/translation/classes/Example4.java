class Example4 {
	public static void main(String[] a) {
		System.out.println(new Support4().Start(5, 3, 2));
	}
}

class Support4 extends Visitor {
	int a;
	boolean ab;

	int Start(int b, int d, int c) {
		boolean ntb;
		int[] nti;
		int ourint;
		Visitor vi;
		/* int ntb; */
		// a = 3;
		nti = new int[a];
		// nti[a-1] = a + 3 ;

		if (5 < 6)
			if (4 < 6) {
				ntb = false;
				ourint = 4 * 3;
			} else {
				if (6 < 8) {
					ntb = true;
					ourint = 5;
				} else {
					ourint = 1 * 5;
				}
			}
		else {
			ourint = 5;
		}

		ntb = ntb;
		a = ourint;
		ab = ntb;
		// vi[ntb]=0;
		a = nti[5];
		System.out.println(a);

		return 0;
	}

}

class Visitor {
	int b;
}
