class addition {
	public static void main(String[] args) {
		int a;
		{
			a = 4 + 1;
			if (!(4 < a)) {
				System.out.println(11);
			} else {
				System.out.println(22);
			}
		}
		System.out.println(a);
	}
}
