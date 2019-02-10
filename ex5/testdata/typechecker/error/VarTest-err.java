class VarTest {
	public static void main(String[] a){
    }
}

class A {
    boolean m() {
        return x;  // TE
    }
}

class B extends A {
}

class C extends B {
    boolean x;
}