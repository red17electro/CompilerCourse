class Example3 {
	public static void main(String[] args) {
		System.out.println(new Support().m());
	}
}

class Support {
	int m() {
		Support a;
		int b;
		int c;
		a = new Support();
		b = this.met(new Support());
		return new Support().f();
	}

	int f() {
		return 10;
	}

	int g() {
		return 7;
	}

	int met(Support o) {
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