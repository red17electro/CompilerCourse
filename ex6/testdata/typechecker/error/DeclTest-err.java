class DeclTest {
	public static void main(String[] a){
    }
}

class A {
    
    A m(boolean x)
    {    
        return new A();
    }

    A m() { // TE
        return new A();
    }
}

