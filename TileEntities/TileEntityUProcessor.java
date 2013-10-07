/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ReactorCraft.TileEntities;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import Reika.DragonAPI.Instantiable.HybridTank;
import Reika.DragonAPI.Instantiable.ParallelTicker;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.ReactorCraft.ReactorCraft;
import Reika.ReactorCraft.Base.TileEntityInventoriedReactorBase;
import Reika.ReactorCraft.Registry.ReactorItems;
import Reika.ReactorCraft.Registry.ReactorOres;
import Reika.ReactorCraft.Registry.ReactorTiles;

public class TileEntityUProcessor extends TileEntityInventoriedReactorBase implements IFluidHandler {

	public static final int ACID_PER_UNIT = 125;
	public static final int ACID_PER_FLUORITE = 250;

	private HybridTank output = new HybridTank("uprocout", 3000);
	private HybridTank acid = new HybridTank("uprochf", 3000);
	private HybridTank water = new HybridTank("uprocwater", 3000);

	private ItemStack[] inv = new ItemStack[3];

	public int HF_timer;
	public int UF6_timer;

	public static final int ACID_TIME = 80;
	public static final int UF6_TIME = 400;

	private ParallelTicker timer = new ParallelTicker().addTicker("acid", ACID_TIME).addTicker("uf6", UF6_TIME);

	@Override
	public int getIndex() {
		return ReactorTiles.PROCESSOR.ordinal();
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		this.getWaterBuckets();
		if (this.canMakeAcid()) {
			timer.updateTicker("acid");
			if (timer.checkCap("acid"))
				this.makeAcid();
		}
		else {
			timer.resetTicker("acid");
		}

		if (this.canMakeUF6()) {
			timer.updateTicker("uf6");
			if (timer.checkCap("uf6"))
				this.makeUF6();
		}
		else {
			timer.resetTicker("uf6");
		}

		if (!world.isRemote) {
			HF_timer = timer.getTickOf("acid");
			UF6_timer = timer.getTickOf("uf6");
		}
	}

	public boolean canMakeUF6() {
		return (this.hasUranium()) && this.getHF() > ACID_PER_UNIT && this.canAcceptMoreUF6(FluidContainerRegistry.BUCKET_VOLUME);
	}

	private boolean hasUranium() {
		if (inv[2] == null)
			return false;
		if (ReikaItemHelper.matchStacks(inv[2], ReactorOres.PITCHBLENDE.getProduct()))
			return true;
		ArrayList<ItemStack> ingots = OreDictionary.getOres("ingotUranium");
		return ReikaItemHelper.listContainsItemStack(ingots, inv[2]);
	}

	public boolean canMakeAcid() {
		return this.getWater() > 0 && inv[0] != null && inv[0].itemID == ReactorItems.FLUORITE.getShiftedItemID() && this.canAcceptMoreHF(ACID_PER_FLUORITE);
	}

	private void makeAcid() {
		ReikaInventoryHelper.decrStack(0, inv);
		this.addHF(ACID_PER_FLUORITE);
		water.drain(ACID_PER_FLUORITE, true);
	}

	private void makeUF6() {
		ReikaInventoryHelper.decrStack(2, inv);
		output.fill(FluidRegistry.getFluidStack("uranium hexafluoride", FluidContainerRegistry.BUCKET_VOLUME), true);
		acid.drain(ACID_PER_UNIT, true);
	}

	public int getHFTimerScaled(int p) {
		return (int)(p*timer.getPortionOfCap("acid"));
	}

	public int getUF6TimerScaled(int p) {
		return (int)(p*timer.getPortionOfCap("uf6"));
	}

	public int getWaterScaled(int p) {
		return p*this.getWater()/water.getCapacity();
	}

	public int getHFScaled(int p) {
		return p*this.getHF()/acid.getCapacity();
	}

	public int getUF6Scaled(int p) {
		return p*this.getUF6()/output.getCapacity();
	}

	public int getWater() {
		return water.getFluid() != null ? water.getFluid().amount : 0;
	}

	public int getHF() {
		return acid.getFluid() != null ? acid.getFluid().amount : 0;
	}

	public int getUF6() {
		return output.getFluid() != null ? output.getFluid().amount : 0;
	}

	private void getWaterBuckets() {
		if (inv[1] != null && inv[1].itemID == Item.bucketWater.itemID && this.canAcceptMoreWater(FluidContainerRegistry.BUCKET_VOLUME)) {
			water.fill(FluidRegistry.getFluidStack("water", FluidContainerRegistry.BUCKET_VOLUME), true);
			inv[1] = new ItemStack(Item.bucketEmpty);
		}
	}

	public boolean canAcceptMoreWater(int amt) {
		return water.getFluid() == null || water.getFluid().amount+amt <= water.getCapacity();
	}

	public boolean canAcceptMoreHF(int amt) {
		return acid.getFluid() == null || acid.getFluid().amount+amt <= acid.getCapacity();
	}

	public boolean canAcceptMoreUF6(int amt) {
		return output.getFluid() == null || output.getFluid().amount+amt <= output.getCapacity();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return 3;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv[i];
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv[i] = itemstack;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		if (ReikaItemHelper.matchStacks(is, ReactorOres.PITCHBLENDE.getProduct()))
			return i == 2;
		if (ReikaItemHelper.listContainsItemStack(OreDictionary.getOres("ingotUranium"), is))
			return i == 2;
		if (is.itemID == ReactorItems.FLUORITE.getShiftedItemID())
			return i == 0;
		if (is.itemID == Item.bucketWater.itemID)
			return i == 1;
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (!this.canFill(from, resource.getFluid()))
			return 0;
		return water.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return output.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return output.drain(resource.amount, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return fluid.equals(FluidRegistry.WATER) || fluid.equals(ReactorCraft.HF);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return fluid.equals(ReactorCraft.UF6);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{water.getInfo(), acid.getInfo(), output.getInfo()};
	}

	public void addHF(int amt) {
		int a = acid.fill(FluidRegistry.getFluidStack("hydrofluoric acid", amt), true);
	}

	@Override
	public void readFromNBT(NBTTagCompound NBT)
	{
		super.readFromNBT(NBT);

		UF6_timer = NBT.getInteger("uf6");
		HF_timer = NBT.getInteger("hf");

		NBTTagList nbttaglist = NBT.getTagList("Items");
		inv = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
			byte byte0 = nbttagcompound.getByte("Slot");

			if (byte0 >= 0 && byte0 < inv.length)
			{
				inv[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}

		water.readFromNBT(NBT);
		acid.readFromNBT(NBT);
		output.readFromNBT(NBT);
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound NBT)
	{
		super.writeToNBT(NBT);

		NBT.setInteger("uf6", UF6_timer);
		NBT.setInteger("hf", HF_timer);

		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < inv.length; i++)
		{
			if (inv[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				inv[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		NBT.setTag("Items", nbttaglist);

		water.writeToNBT(NBT);
		acid.writeToNBT(NBT);
		output.writeToNBT(NBT);
	}

	public int getFluid(FluidStack liquid) {
		if (liquid.getFluid().equals(FluidRegistry.WATER))
			return this.getWater();
		if (liquid.getFluid().equals(ReactorCraft.HF))
			return this.getHF();
		if (liquid.getFluid().equals(ReactorCraft.UF6))
			return this.getUF6();
		return 0;
	}
}