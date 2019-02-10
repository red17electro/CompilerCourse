/** And in a while condition */
class Main {
	public static void main(String[] args) {
		int i;
		boolean doContinue;
		doContinue = true;
		i = 0;
		System.out.println(100);
		while (doContinue) { // three iterations
			System.out.println(i);
			i = i + 1;
			doContinue = i < 3;
		}
		System.out.println(101);
	}
}