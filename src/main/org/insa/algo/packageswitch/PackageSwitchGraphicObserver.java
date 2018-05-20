package org.insa.algo.packageswitch;

import org.insa.graph.Node;
import org.insa.graphics.drawing.Drawing;
import org.insa.graphics.drawing.overlays.PointSetOverlay;

import java.awt.*;

public class PackageSwitchGraphicObserver implements PackageSwitchObserver {

    private final Drawing drawing;
    private final PointSetOverlay psOverlay;

    public PackageSwitchGraphicObserver(Drawing drawing) {
        this.drawing = drawing;

        this.psOverlay = drawing.createPointSetOverlay();
    }

    @Override
    public void notifyNodeReached(Node node, int count) {

        switch (count) {

            case 1:
                psOverlay.setColor(Color.CYAN);
                break;

            case 2:
                psOverlay.setColor(Color.BLUE);
                break;

            case 3:
                psOverlay.setColor(Color.PINK);
                break;

            default:
                psOverlay.setColor(Color.MAGENTA);
        }

        psOverlay.addPoint(node.getPoint());

    }

    @Override
    public void notifyMeetingReached(Node node) {
        psOverlay.delete();
        drawing.drawMarker(node.getPoint(), Color.LIGHT_GRAY, Color.DARK_GRAY, Drawing.AlphaMode.OPAQUE);
    }
}
