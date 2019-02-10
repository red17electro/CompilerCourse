/**
 * The this-object
 * 
 * @author anton
 *
 */
class ThisTest {
	public static void main(String[] args) {
		Bla bla;
		Bla blub;
		int dumm;
		bla = new Bla();
		System.out.println(1);
		dumm = bla.setFeld(2);
		blub = bla.getThis();
		dumm = bla.setFeld(3);
		dumm = blub.printFeld();
		System.out.println(4);
	}
}

class Bla {
	int feld;

	int setFeld(int x) {
		feld = x;
		return 0;
	}

	Bla getThis() {
		return this;
	}

	int printFeld() {
		return this.doPrintFeld();
	}

	int doPrintFeld() {
		System.out.println(feld);
		return 0;
	}
}
