class Main {
	public static void main(String[] args) {
		int i;
		int[] arr;
		i = 0;
		arr = new int[3];
		while (true) {
			arr[i] = 0; // will ERROR later to break out of the loop
			System.out.println(i);
			i = i + 1;
		}
	}
}