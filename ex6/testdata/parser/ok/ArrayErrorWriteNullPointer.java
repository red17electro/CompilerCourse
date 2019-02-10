class Main {
	public static void main(String[] args) {
		Bla bla;
		int dummy;
		bla = new Bla();
		System.out.println(1);
		dummy = bla.writeUninitializedArray();
		System.out.println(2);
	}
}

class Bla {
	int[] array;

	int readUninitializedArray() {
		return array[0];
	}

	int writeUninitializedArray() {
		array[0] = 1;
		return 1;
	}

	int lengthOfUninitializedArray() {
		return array.length;
	}
}