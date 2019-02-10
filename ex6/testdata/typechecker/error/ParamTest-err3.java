class ParamTest {
	public static void main(String[] a){
            A x;
            B y;
            x = new A();
            y = new B();
            y = x.m(y); // TE 
	}
}

class A {
    A m(A x)
    {    
        return new A();
    }

}

class B extends A {}
