package keystrokesmod.module.impl.world.scaffold.schedule;

import keystrokesmod.event.JumpEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSchedule;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class TellySchedule extends IScaffoldSchedule {
    private final SliderSetting straightTicks;
    private final SliderSetting diagonalTicks;
    private final SliderSetting jumpDownTicks;

    private boolean noPlace = false;

    public TellySchedule(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(straightTicks = new SliderSetting("Straight ticks", 6, 0, 8, 1));
        this.registerSetting(diagonalTicks = new SliderSetting("Diagonal ticks", 4, 0, 8, 1));
        this.registerSetting(jumpDownTicks = new SliderSetting("Jump down ticks", 1, 0, 8, 1));
    }

    @Override
    public boolean noPlace() {
        return noPlace || mc.thePlayer.onGround && parent.placeBlock == null;
    }

    @Override
    public boolean noRotation() {
        return noPlace();
    }

    @Override
    public void onEnable() throws Throwable {
        noPlace = false;
    }

    @SubscribeEvent
    public void onScaffoldPlace(ScaffoldPlaceEvent event) {
        if (noPlace)
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public boolean onPreUpdate(PreUpdateEvent event) {
        if (parent.offGroundTicks == 0) {
            if (parent.onGroundTicks == 0)
                noPlace = true;
            else if (MoveUtil.isMoving() && !Utils.jumpDown())
                mc.thePlayer.jump();
        } else if (BlockUtils.insideBlock(mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX, mc.thePlayer.motionY + 0.1, mc.thePlayer.motionZ))) {
            noPlace = true;
        } else {
            if (Utils.jumpDown()) {
                if (parent.offGroundTicks >= (int) jumpDownTicks.getInput()) {
                    noPlace = false;
                }
            } else {
                if (parent.isDiagonal()) {
                    if (parent.offGroundTicks == (int) diagonalTicks.getInput()) {
                        noPlace = false;
                    }
                } else {
                    if (parent.offGroundTicks == (int) straightTicks.getInput()) {
                        noPlace = false;
                    }
                }
            }
        }

        return true;
    }

    @SubscribeEvent
    public void onJump(JumpEvent event) {
        if (parent.offGroundTicks == 0 && parent.onGroundTicks == 0)
            event.setCanceled(true);
    }
}
