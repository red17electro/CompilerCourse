// Should return 5

class Scope2 {
	public static void main(String[] argv) {
		System.out.println(new S().run(5, 10));
	}
}

class S {

	int n;

	int Init(int m) {
		n = m;
		return 0;
	}

	int run(int n, int m) {
		int k;
		k = this.Init(m);
		return n;
	}

}