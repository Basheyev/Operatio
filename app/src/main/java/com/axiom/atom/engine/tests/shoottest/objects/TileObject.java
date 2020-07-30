package com.axiom.atom.engine.tests.shoottest.objects;


import com.axiom.atom.engine.core.GameObject;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Rectangle;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.physics.PhysicsRender;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.tests.shoottest.ShooterScene;


public class TileObject extends GameObject {
  //  protected Sprite sprite;
    protected TileMap tilemap;
    protected int column = 0;
    protected int row = 0;
    protected int tileType = 0;

    private Rectangle debugRectangle;

    public TileObject(GameScene gameScene, TileMap tilemap, Sprite sprite, int tileType) {
        super(gameScene);
        float widthHalf = sprite.getWidth() / 2;
        float heightHalf = sprite.getHeight() / 2;
        bodyType = PhysicsRender.BODY_STATIC;
        this.sprite = sprite;
        sprite.zOrder = 1;

        this.tileType = tileType;
        this.mass = 1;
        this.tilemap = tilemap;
        localBounds = new AABB(-widthHalf,-heightHalf,widthHalf,widthHalf);

        debugRectangle = new Rectangle();
        debugRectangle.zOrder = 2;
    }

    /**
     *
     * @param camera
     */
    public void draw(Camera camera) {
        sprite.setActiveFrame(tileType);
        sprite.draw(camera,x,y,scale);
        if (tileType>0 & ((ShooterScene)scene).debug) {
            AABB bounds = getWorldBounds();
            debugRectangle.draw(camera, bounds.min.x, bounds.min.y, bounds.width, bounds.height);
            debugRectangle.setColor(0.3f, 0.5f, 0.9f, 0.5f);
        }
    }


    @Override
    public String toString() {
        return "Tile (pos:" + x + "," + y + " vel:" + velocity.x + "," + velocity.y + ")";
    }

    public TileMap getTilemap() {
        return tilemap;
    }

    @Override
    public void update(float deltaTime) {
        debugRectangle.setColor(0.3f, 0.5f, 0.9f, 0.5f);
    }

    @Override
    public void onCollision(GameObject object) {
        debugRectangle.setColor(0.9f, 0.5f, 0.3f, 0.5f);
    }

}
