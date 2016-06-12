package com.mygdx.game.Generator;

import com.badlogic.gdx.physics.box2d.*;

/**
 * Created by patryk on 2016-05-22.
 */
public class ShapeGenerator {
    private World box2DWorld;

    public ShapeGenerator(World box2DWorld) {
        this.box2DWorld = box2DWorld;
    }

    public Body generateShape( float poxX, float posY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(poxX, posY);

        Body generatedBody = box2DWorld.createBody(bodyDef);

        PolygonShape square = new PolygonShape();
        square.setAsBox(0.2f, 0.2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = square;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        generatedBody.createFixture(fixtureDef);
        square.dispose();

        return generatedBody;
    }
}