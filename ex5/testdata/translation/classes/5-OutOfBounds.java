class Main {
    public static void main(String[] a) {
	System.out.println(new A().run());
    }
}

class A {
     int[] a;

    int run() {
	a = new int[20];
	a[10] = 1;
	System.out.println(a[10]);
	return a[40];
    }
}
