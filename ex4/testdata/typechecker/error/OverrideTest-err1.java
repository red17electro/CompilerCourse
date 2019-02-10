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
    A m (int x) { // TE
        return new A();
    }
}
