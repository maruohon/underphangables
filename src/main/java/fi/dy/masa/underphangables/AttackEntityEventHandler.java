package fi.dy.masa.underphangables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Items;
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
        if (event.entityPlayer.capabilities.isCreativeMode == true ||
            event.target.isEntityInvulnerable(DamageSource.causePlayerDamage(event.entityPlayer)) == true)
        {
            return;
        }

        if (event.target instanceof EntityItemFrame)
        {
            EntityItemFrame entityItemFrame = (EntityItemFrame)event.target;
            ItemStack stack = entityItemFrame.getDisplayedItem();

            if (event.target.worldObj.isRemote == false)
            {
                if (stack != null)
                {
                    if (event.target.worldObj.getGameRules().getBoolean("doEntityDrops") == true)
                    {
                        dropDisplayItemFromItemFrame(entityItemFrame, stack, getEntityYawFacing(event.target));
                    }

                    entityItemFrame.setDisplayedItem(null);
                }
                else
                {
                    if (event.target.worldObj.getGameRules().getBoolean("doEntityDrops") == true)
                    {
                        dropItemWithAdjustedPosition(new ItemStack(Items.item_frame), entityItemFrame, getEntityYawFacing(event.target));
                    }

                    entityItemFrame.setDead();
                }
            }

            event.setCanceled(true);
        }
        else if (event.target instanceof EntityPainting)
        {
            if (event.target.worldObj.isRemote == false)
            {
                if (event.target.worldObj.getGameRules().getBoolean("doEntityDrops") == true)
                {
                    dropItemWithAdjustedPosition(new ItemStack(Items.painting), event.target, getEntityYawFacing(event.target));
                }

                event.target.setDead();
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
        EntityItem entityItem = createEntityItemWithoutHorizontalMotion(stack, entity.worldObj, posX, entity.posY, posZ, 0.1d);

        //System.out.println("adjusting towards " + facingToAdjustTo);
        //System.out.println(String.format("post x: %.2f y: %.2f z: %.2f", entityItem.posX, entityItem.posY, entityItem.posZ));

        if (entity.captureDrops)
        {
            entity.capturedDrops.add(entityItem);
        }
        else
        {
            entity.worldObj.spawnEntityInWorld(entityItem);
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

        if (stack != null && stack.stackSize > 0 && stack.getItem() != null && entityItemFrame.worldObj.rand.nextFloat() < dropChance)
        {
            stack = stack.copy();
            try
            {
                Method method = ReflectionHelper.findMethod(EntityItemFrame.class, entityItemFrame, new String[] {"func_110131_b", "removeFrameFromMap"}, ItemStack.class);
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
