class MainClass{
    public static void main(String[] args){
        Blub b;
        int a;
        boolean c;
        b = new Blub();
        b.blah();
        new Blub();
        b.bleh(4);
    }
}

class Blub {
    boolean blah(){return false;}
    int bleh(int a){return a;}
}
