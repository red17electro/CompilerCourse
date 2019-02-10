class Arrays {
	public static void main(String[] args) {
		A a;
		a = new A();
		System.out.println(a.print());
		System.out.println(a.sort());
		System.out.println(a.laenge());
	}

}

class A {

	int[] g;

	int print() {
		int[] array;
		array = new int[3];
		array[0] = 5;
		array[1] = 6;
		System.out.println(array[0]);
		System.out.println(array[1]);
		return 0;
	}

	int sort() {
		int[] array;
		array = new int[3];
		array[0] = 6;
		array[1] = 5;
		System.out.println(array[0]);
		System.out.println(array[1]);
		return 0;
	}

	int laenge() {
		int[] array2;
		array2 = new int[7];
		g = new int[9];
		g[5] = 99;
		array2[2] = 22;
		return array2[2];
	}
}
