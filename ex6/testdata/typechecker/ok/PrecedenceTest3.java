class PrecedenceTest{
    public static void main(String[] a){
	C x;
        x = new C().m().m().m();
	}
}

class C {
    C m() {
        return new C();
    }
}
