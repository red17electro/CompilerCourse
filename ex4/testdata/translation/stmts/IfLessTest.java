/**
 * Test if-statement and less-operator.
 * 
 * @author anton
 *
 */
class Main {
	public static void main(String[] args) {
		if (1 < 2) {
			System.out.println(1);
		} else {
			System.out.println(666);
		}
		if (2 < 1) {
			System.out.println(999);
		} else {
			System.out.println(2);
		}
		if (1 < 1) {
			System.out.println(999);
		} else {
			System.out.println(3);
		}
		System.out.println(4);
	}
}