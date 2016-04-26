package test;

import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import org.jbox2d.dynamics.Body;


public class RectangleActor extends Actor {

	public RectangleActor(Body body, Rectangle sprite) {
		//�ndra Actor till javaFX.Shape ist�llet f�r Node
		super(body, sprite);
	}
	
	
	protected void updatePos() {
		//L�gg till - height/2 och - width/2
		sprite.setLayoutX(body.getPosition().x * Val.RATIO);
		sprite.setLayoutY(body.getPosition().y * Val.RATIO);
		sprite.setRotate(body.getAngle() * Math.PI / 180);
	}

}
