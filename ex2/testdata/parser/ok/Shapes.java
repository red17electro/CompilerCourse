class Main {
	public static void main(String[] a){
		Circle c;
		Rect r;
		Shape s;
		c = new Circle().init(0, 0, 100);
		r = new Rect().init(0, 0, 100, 50);
		s = c;
		System.out.println(s.area());
		s = r;
		System.out.println(s.area());
	}
}


class Shape {
	int x;
	int y;
	
	int area() {
		return 0;
	}
}

class Circle extends Shape {
	int radius;
	
	Circle init(int px, int py, int pradius) {
		x = px;
		y = py;
		radius = pradius;
		return this;
	}
	
	int area() {
		return radius*radius*3;
	}
}

class Rect extends Shape {
	int width;
	int height;
	
	Rect init(int px, int py, int pwidth, int pheight) {
		x = px;
		y = py;
		width = pwidth;
		height = pheight;
		return this;
	}
	
	int area() {
		return width*height;
	}
}