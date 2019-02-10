/**
 * Not-operator in an expression, not in the condition of an if.
 * 
 * @author anton
 *
 */
class NotExpr {
	public static void main(String[] args) {
		boolean a;
		boolean b;
		a = true;
		b = !a;
		if (b) {
			System.out.println(666);
		} else {
			System.out.println(1);
		}
	}
}