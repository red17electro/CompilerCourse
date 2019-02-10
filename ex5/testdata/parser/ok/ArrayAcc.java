class ArrayAcc {
	public static void main(String[] msdf) {
		int[] a;
		int i;
		int u;
		u = 5;
		// OK
		a = new int[u];
		i = 0;
		while (i < u) {
			a[i] = i + 1;
			i = i + 1;
		}
		i = 0;
		while (i < u * 2) {
			System.out.println(a[i]);
			i = i + 1;
		}
	}
}