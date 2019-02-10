class MethodTest {
	public static void main(String[] a){
            A x;
            x = new A();
            x = x.m();
	}
}

class A {
    B m()
    {    
        return new A(); // TE
    }

}

class B extends A {}