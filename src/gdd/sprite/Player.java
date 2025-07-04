package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 100;
    // private int width;
    private int frame = 0;
    private boolean isFiring = false;

    public static final int DIR_LEFT = 0;
    public static final int DIR_RIGHT = 1;
    private int facing = 0;

    private static final int ACT_STANDING = 0;
    private static final int ACT_RUNNING = 1;
    private static final int ACT_JUMPING = 2;
    private int action = ACT_STANDING;

    private int clipNo = 0;
    private final Rectangle[] clips = new Rectangle[] {
            new Rectangle(18, 20, 80, 90), // 0: stand still
            new Rectangle(110, 20, 80, 90), // 1: stand blink
            new Rectangle(294, 20, 90, 90), // 2: run 1
            new Rectangle(400, 20, 60, 90), // 3: run 2
            new Rectangle(470, 20, 80, 90), // 4: run 3
            new Rectangle(138, 230, 100, 110), // 5: jump 1, no firing
            new Rectangle(18, 230, 100, 110), // 6: jump 2, firing
            new Rectangle(128, 124, 124, 94), // 7: stand Shoot
            new Rectangle(248, 120, 118, 94), // 8: run shoot 1
            new Rectangle(372, 120, 118, 94), // 9: run shoot 2
            new Rectangle(486, 120, 118, 94), // 10: run shoot 3
    };

    public Player() {
        initPlayer();
    }

    public int getFrame() {
        return frame;
    }

    @Override
    public int getHeight() {
        return clips[clipNo].height;
    }

    public int getFacing() {
        return facing;
    }

    @Override
    public int getWidth() {
        return clips[clipNo].width;
    }

    @Override
    public Image getImage() {
        Rectangle bound = clips[clipNo];
        // TODO this can be cached.
        BufferedImage bImage = toBufferedImage(image);
        return bImage.getSubimage(bound.x, bound.y, bound.width, bound.height);
    }

    private void initPlayer() {
        var ii = new ImageIcon(IMG_PLAYER);
        setImage(ii.getImage());

        setX(START_X);
        // Modify this line to position the player on the ground
        setY(GROUND);
    }

    // Add these variables at the class level
    private int jumpCount = 0;
    private boolean isJumping = false;
    private boolean isFireKeyDown = false; // New variable to track if fire key is held down

    // Add these variables for shooting (keep the existing isFiring variable)
    private int firingTimer = 0;
    private static final int FIRING_DURATION = 20; // Duration of firing animation

    public void act() {
        System.out.printf("Player action=%d frame=%d facing=%d\n", action, frame, facing);

        frame++;

        // Update firing timer if player is firing
        if (isFiring) {
            firingTimer++;
            if (firingTimer >= FIRING_DURATION) {
                isFiring = false;
                firingTimer = 0;

                // If fire key is still being held down, start firing again
                if (isFireKeyDown) {
                    isFiring = true;
                }
            }
        }

        switch (action) {
            case ACT_STANDING:
                if (isFiring) {
                    clipNo = 7; // standing shoot
                } else {
                    if (clipNo == 1 && frame > 5) { // blink only one frame
                        frame = 0;
                        clipNo = 0;
                    }
                    if (frame > 40) { // blink
                        frame = 0;
                        clipNo = 1; // blink
                    }
                }
                // Reset jump state when standing
                isJumping = false;
                jumpCount = 0;
                break;

            case ACT_RUNNING:
                if (isFiring) { // running and shooting animation
                    if (frame <= 10) {
                        clipNo = 8; // run shoot 1
                    } else if (frame <= 20) {
                        clipNo = 9; // run shoot 2
                    } else if (frame <= 30) {
                        clipNo = 8; // run shoot 3
                    } else if (frame <= 40) {
                        clipNo = 10; // run shoot 4
                    } else {
                        clipNo = 8; // run shoot 1
                        frame = 0;
                    }
                } else {
                    // Normal running animation
                    if (frame <= 10) {
                        clipNo = 3;
                    } else if (frame <= 20) {
                        clipNo = 2;
                    } else if (frame <= 30) {
                        clipNo = 3;
                    } else if (frame <= 40) {
                        clipNo = 4;
                    } else {
                        clipNo = 3;
                        frame = 0;
                    }
                }
                // Reset jump state when running
                isJumping = false;
                jumpCount = 0;
                break;
            case ACT_JUMPING:
                if (isFiring) {
                    clipNo = 6; // jump firing
                } else {
                    clipNo = 5; // normal jump
                }
                break;
        }

        x += dx;
        y += dy;

        // Apply gravity and handle landing
        if (action == ACT_JUMPING) {
            dy += 1; // Gravity effect

            // Check if player has landed on the ground
            if (y >= GROUND) {
                y = GROUND; // Return to ground position
                dy = 0;

                // Check if player is moving horizontally when landing
                if (dx != 0) {
                    // If moving, set action to running
                    action = ACT_RUNNING;
                    frame = 0;
                    clipNo = 3; // Start with running animation frame
                } else {
                    // If not moving, set action to standing
                    action = ACT_STANDING;
                    frame = 0;
                    clipNo = 0;
                }

                isJumping = false;
                jumpCount = 0;
            }
        }

        if (x <= 2) {
            x = 2;
        }

        if (x >= BOARD_WIDTH - 2 * width) {
            x = BOARD_WIDTH - 2 * width;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            if (action != ACT_RUNNING && action != ACT_JUMPING) {
                // Change of action only if not jumping
                frame = 0;
                action = ACT_RUNNING;
            }
            facing = DIR_LEFT;
            dx = -2;

            // If already jumping, just change direction but keep jumping action
            if (action == ACT_JUMPING) {
                // Keep the jumping action but change horizontal direction
                dx = -2;
            }
        } else if (key == KeyEvent.VK_RIGHT) {
            if (action != ACT_RUNNING && action != ACT_JUMPING) {
                // Change of action only if not jumping
                frame = 0;
                action = ACT_RUNNING;
            }
            facing = DIR_RIGHT;
            dx = 2;

            // If already jumping, just change direction but keep jumping action
            if (action == ACT_JUMPING) {
                // Keep the jumping action but change horizontal direction
                dx = 2;
            }
        } else if (key == KeyEvent.VK_SPACE) {
            // Handle jumping with space bar
            if (!isJumping) {
                action = ACT_JUMPING;
                dy = -18;
                isJumping = true;
                jumpCount = 1;
            }
        } else if (key == KeyEvent.VK_F) {
            // Handle shooting with F key
            isFireKeyDown = true;
            isFiring = true;
            firingTimer = 0;

            // Create a bullet here
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            if (action != ACT_STANDING && action != ACT_JUMPING) {
                // Change of action only if not jumping
                clipNo = 0;
                frame = 0;
                action = ACT_STANDING;
            }
            facing = DIR_LEFT;
            dx = 0;

            // If jumping, make sure player will land at original position
            if (action == ACT_JUMPING) {
                // Player will keep the jumping action but stop horizontal movement
                dx = 0;
            }
        }

        if (key == KeyEvent.VK_RIGHT) {
            if (action != ACT_STANDING && action != ACT_JUMPING) {
                // Change of action only if not jumping
                clipNo = 0;
                frame = 0;
                action = ACT_STANDING;
            }
            facing = DIR_RIGHT;
            dx = 0;

            // If jumping, make sure player will land at original position
            if (action == ACT_JUMPING) {
                // keep the jumping action but stop horizontal movement
                dx = 0;
            }
        }

        if (key == KeyEvent.VK_F) {
            // Stop continuous firing when F key is released
            isFireKeyDown = false;
        }
    }
}
