class OverrideTest {
	public static void main(String[] a){
    }
}

class A {
    A m(boolean x)
    {    
        return new A();
    }
}

class B extends A{
    int m (boolean x) { // TE
        return 0;
    }
}
