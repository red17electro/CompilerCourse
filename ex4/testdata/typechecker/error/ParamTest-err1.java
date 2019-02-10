class ParamTest {
	public static void main(String[] a){
            A x;
            x = new A();
            x = x.m(0,false); //TE
	}
}

class A {
    A m(boolean b,int i)
    {    
        return new A();
    }

}
