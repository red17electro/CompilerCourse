/**
 * Instance method calls with one parameter and one return value. Field access
 * read, write. No overriding yet.
 * 
 * @author anton
 *
 */
class InstanceMethodCall {
	public static void main(String[] args) {
		Zahl z;
		int ignore;
		z = new Zahl();
		ignore = z.set(1);
		System.out.println(z.get());
		ignore = z.set(2);
		ignore = z.print();
	}
}

class Zahl {
	int theZahl;

	int set(int x) {
		theZahl = x;
		return 667;
	}

	int get() {
		return theZahl;
	}

	int print() {
		System.out.println(theZahl);
		return 666;
	}
}