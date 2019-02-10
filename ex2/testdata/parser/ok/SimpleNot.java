/**
 * Test the not-expression, without observing side-effects.
 * 
 * @author anton
 *
 */
class SimpleNot {
	public static void main(String[] args) {
		if (!(1 < 2)) {
			System.out.println(666);
		} else {
			System.out.println(1);
		}
		if (!(2 < 1)) {
			System.out.println(2);
		} else {
			System.out.println(999);
		}
	}
}