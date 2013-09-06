/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ReactorCraft.GUIs;

import net.minecraft.entity.player.EntityPlayer;
import Reika.ReactorCraft.Base.ReactorGuiBase;
import Reika.ReactorCraft.Container.ContainerFuelRod;
import Reika.ReactorCraft.TileEntities.TileEntityFuelRod;

public class GuiFuelRod extends ReactorGuiBase {

	public GuiFuelRod(EntityPlayer player, TileEntityFuelRod fuel) {
		super(new ContainerFuelRod(player, fuel), player, fuel);
		ySize = 182;
	}

	@Override
	public String getGuiTexture() {
		return "fuelrod";
	}

}
