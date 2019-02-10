class Shapes {
	public static void main(String[] args) {
		Shape shape;
		Rectangle rect;
		Circle circ;
		boolean b;
		b = false;
		if (b) {
			rect = new Rectangle();
			rect.height = 10;
			rect.width = 20;
			shape = rect;
		} else {
			circ = new Circle();
			circ.radius = 2;
			shape = circ;
		}
		shape.shapeNumber = 77;
		System.out.println(shape.getScaledArea(3));
	}
}

class Shape {
	int shapeNumber;

	int getArea() {
		return 0;
	}

	int getScaledArea(int scale) {
		return scale * this.getArea();
	}
}


class Rectangle extends Shape {
	int width;
	int height;

	int getArea() {
		return width*height;
	}

	boolean contains(int x, int y) {
		return x < width+1 && y < height+1;
	}
}

class Circle extends Shape {
	int radius;

	int getArea() {
		return radius*radius*3;
	}
}
