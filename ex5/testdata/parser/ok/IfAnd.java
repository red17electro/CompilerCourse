class IfAnd {
	public static void main(String[] args) {
		boolean yes;
		boolean si;
		boolean nie;
		boolean ikke;
		yes = true;
		si = true;
		nie = false;
		ikke = false;
		if (yes && si) {
			System.out.println(1);
		} else {
			System.out.println(666);
		}
		if (yes && nie) {
			System.out.println(667);
		} else {
			System.out.println(2);
		}
		if (nie && si) {
			System.out.println(668);
		} else {
			System.out.println(3);
		}
		if (ikke && nie) {
			System.out.println(669);
		} else {
			System.out.println(4);
		}
		// Nested!
		if ((yes && si) && (si && yes)) {
			System.out.println(5);
		} else {
			System.out.println(670);
		}
		if ((yes && nie) && (si && yes)) {
			System.out.println(671);
		} else {
			System.out.println(6);
		}
		if ((yes && si) && (nie && yes)) {
			System.out.println(672);
		} else {
			System.out.println(7);
		}
		if ((yes && si) && (yes && nie)) {
			System.out.println(673);
		} else {
			System.out.println(8);
		}
	}
}