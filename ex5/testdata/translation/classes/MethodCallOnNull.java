/**
 * Call method on a null instance. Expect ERROR.
 * 
 * @author anton
 *
 */
class MethodCallOnNull {
	public static void main(String[] args) {
		Bla z;
		int x;
		System.out.println(1);
		z = new Bla();
		System.out.println(2);
		x = z.eins();
	}
}

class Bla {
	Bla field;

	int eins() {
		System.out.println(3);
		return field.zwei(); // field=null, but we don't know that yet
	}

	int zwei() {
		System.out.println(666);
		return 2;
	}
}
