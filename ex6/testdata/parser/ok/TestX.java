class TestX {

	public static void main(String[] args) {
		System.out.println(new Support().m());
	}

}

class Support {

	int m() {
		// Support a;
		int b;
		// int c;
		// a = new Support();
		b = this.met();
		return 99;
	}

	int g() {
		return 7;
	}

	int met() {
		K k;
		k = this.boom(new C());
		k = this.loom(new C());
		return k.g();
	}

	K boom(K k) {
		return k;
	}

	K loom(C c) {
		return c;
	}

}

class K extends Support {

}

class C extends K {

}
