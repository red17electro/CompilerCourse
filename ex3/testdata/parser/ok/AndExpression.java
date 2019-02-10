/** And in an expression context */
class Main {
	public static void main(String[] args) {
		boolean z;
		boolean wahr;
		wahr = true;
		z = wahr && false;
		if (z) {
			System.out.println(666);
		} else {
			System.out.println(1);
		}

		z = wahr && true;
		if (z) {
			System.out.println(2);
		} else {
			System.out.println(667);
		}
	}
}
