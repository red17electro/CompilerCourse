class ArrayOk {

	public static void main(String[] args) {
		int[] arr;
		arr = new int[4];
		arr[0] = 100;
		arr[1] = 101;
		arr[2] = 102;
		arr[3] = 103;
		System.out.println(arr[0]);
		System.out.println(arr[1]);
		System.out.println(arr[2]);
		System.out.println(arr[3]);
		System.out.println(arr.length); // should not have been overwritten!
	}
}
