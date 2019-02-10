class Arrays {
	public static void main(String[] args) {
		A a;
		a = new A();
		System.out.println(a.print(5));
		System.out.println(a.sort());
		System.out.println(a.laenge());
	}

}

class A {
	int[] array;

	int print(int s) {
		array = new int[2];
		array[0] = 5;
		array[1] = 6;
		System.out.println(array[0]);
		System.out.println(array[1]);
		return 0;
	}

	int sort() {
		array[0] = 6;
		array[1] = 5;
		System.out.println(array[0]);
		System.out.println(array[1]);
		return 0;
	}

	int laenge() {
		return new int[88 + 88].length;
	}
}
