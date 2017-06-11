package fi.dy.masa.underphangables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

public class AttackEntityEventHandler
{
    @SubscribeEvent
    public void onAttackEntityEvent(AttackEntityEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();

        if (player.capabilities.isCreativeMode || target.isEntityInvulnerable(DamageSource.causePlayerDamage(player)))
        {
            return;
        }

        if (target instanceof EntityItemFrame)
        {
            EntityItemFrame entityItemFrame = (EntityItemFrame)target;
            ItemStack stack = entityItemFrame.getDisplayedItem();

            if (target.getEntityWorld().isRemote == false)
            {
                if (stack.isEmpty() == false)
                {
                    if (target.getEntityWorld().getGameRules().getBoolean("doEntityDrops") == true)
                    {
                        dropDisplayItemFromItemFrame(entityItemFrame, stack, getEntityYawFacing(target));
                    }

                    entityItemFrame.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
                    entityItemFrame.setDisplayedItem(ItemStack.EMPTY);
                }
                else
                {
                    if (target.getEntityWorld().getGameRules().getBoolean("doEntityDrops") == true)
                    {
                        dropItemWithAdjustedPosition(new ItemStack(Items.ITEM_FRAME), entityItemFrame, getEntityYawFacing(target));
                    }

                    entityItemFrame.playSound(SoundEvents.ENTITY_ITEMFRAME_BREAK, 1.0F, 1.0F);
                    entityItemFrame.setDead();
                }
            }

            event.setCanceled(true);
        }
        else if (target instanceof EntityPainting)
        {
            if (target.getEntityWorld().isRemote == false)
            {
                if (target.getEntityWorld().getGameRules().getBoolean("doEntityDrops") == true)
                {
                    dropItemWithAdjustedPosition(new ItemStack(Items.PAINTING), target, getEntityYawFacing(target));
                }

                target.playSound(SoundEvents.ENTITY_ITEMFRAME_BREAK, 1.0F, 1.0F);
                target.setDead();
            }

            event.setCanceled(true);
        }
    }

    public static EnumFacing getEntityYawFacing(Entity entity)
    {
        return EnumFacing.fromAngle(entity.rotationYaw);
    }

    public static EntityItem createEntityItemWithoutHorizontalMotion(ItemStack stack, World world, double x, double y, double z, double motionY)
    {
        EntityItem entityItem = new EntityItem(world, x, y, z, stack);
        entityItem.motionX = 0.0d;
        entityItem.motionY = motionY;
        entityItem.motionZ = 0.0d;

        entityItem.setDefaultPickupDelay();

        return entityItem;
    }

    public static void dropItemWithAdjustedPosition(ItemStack stack, Entity entity, EnumFacing facingToAdjustTo)
    {
        double posX = entity.posX + facingToAdjustTo.getFrontOffsetX() * 0.15f;
        double posZ = entity.posZ + facingToAdjustTo.getFrontOffsetZ() * 0.15f;
        EntityItem entityItem = createEntityItemWithoutHorizontalMotion(stack, entity.getEntityWorld(), posX, entity.posY, posZ, 0.1d);

        //System.out.println("adjusting towards " + facingToAdjustTo);
        //System.out.println(String.format("post x: %.2f y: %.2f z: %.2f", entityItem.posX, entityItem.posY, entityItem.posZ));

        if (entity.captureDrops)
        {
            entity.capturedDrops.add(entityItem);
        }
        else
        {
            entity.getEntityWorld().spawnEntity(entityItem);
        }
    }

    public static void dropDisplayItemFromItemFrame(EntityItemFrame entityItemFrame, ItemStack stack, EnumFacing facing)
    {
        float dropChance = 1.0f;

        try
        {
            dropChance = ReflectionHelper.getPrivateValue(EntityItemFrame.class, entityItemFrame, "field_82337_e", "itemDropChance");
        }
        catch (UnableToAccessFieldException e)
        {
            UnderpHangables.logger.warn("UnableToAccessFieldException while trying to get EntityItemFrame#itemDropChance");
        }

        if (stack.isEmpty() == false && entityItemFrame.getEntityWorld().rand.nextFloat() < dropChance)
        {
            stack = stack.copy();

            try
            {
                Method method = ReflectionHelper.findMethod(EntityItemFrame.class, "removeFrameFromMap", "func_110131_b", ItemStack.class);
                method.invoke(entityItemFrame, stack);
            }
            catch (UnableToFindMethodException e)
            {
                UnderpHangables.logger.warn("UnableToFindMethodException while trying to do EntityItemFrame#removeFrameFromMap()");
            }
            catch (InvocationTargetException e)
            {
                UnderpHangables.logger.warn("InvocationTargetException while trying to do EntityItemFrame#removeFrameFromMap()");
            }
            catch (IllegalAccessException e)
            {
                UnderpHangables.logger.warn("IllegalAccessException while trying to do EntityItemFrame#removeFrameFromMap()");
            }

            dropItemWithAdjustedPosition(stack, entityItemFrame, facing);
        }
    }
}
