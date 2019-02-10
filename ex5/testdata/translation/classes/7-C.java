class Main {
    public static void main(String[] a) {
    	System.out.println(new A().foo(7));
    }
}

class A {

    int foo(int x) {
    	return x+x;
    }
}
