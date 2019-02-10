class Fib {
	public static void main(String[] a) {
		System.out.println(new FibClass().nfib(5));
	}
}

class FibClass {
	int nfib(int num) {
		int res;
		if (num < 2)
			res = 1;
		else
			res = (this.nfib(num - 1)) + (this.nfib(num - 2)) + 1;
		return res;
	}
}
