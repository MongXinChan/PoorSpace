package com.gmail.jobstone.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gmail.jobstone.*;
import com.gmail.jobstone.space.NormalSpace;
import com.gmail.jobstone.space.SpaceGroup;
import com.gmail.jobstone.space.SpaceManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SpaceListener implements Listener {
	
	public SpaceListener (PoorSpace plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void pick(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player) {
			String player = ((Player)e.getEntity()).getName();
			Location loc = e.getItem().getLocation();
			if (!playerpm(player, loc, 6))
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void drop(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		Location loc = e.getPlayer().getLocation();
		if (!playerpm(player.getName(), loc, 6)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在这个空间扔物品的权限！"));
			e.setCancelled(true);
            if (player.getGameMode().equals(GameMode.CREATIVE))
                return;
            Inventory inv = player.getInventory();
            Inventory inv2 = Bukkit.createInventory(null, 36);
            for (int i = 0; i < 36; i++) {
                inv2.setItem(i, inv.getItem(i));
            }
            HashMap<Integer, ItemStack> lostitems = inv2.addItem(e.getItemDrop().getItemStack().clone());
            if (!lostitems.isEmpty()) {
                Message message = new Message(System.currentTimeMillis(), "穷娘", player.getName(), "丢失的物品", "您不小心丢掉了一份物品，请及时查收~");
                message.create(lostitems.get(new Integer(0)));
            }
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void place(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		Location loc = e.getBlockPlaced().getLocation();
		if ((e.getBlock().getType().equals(Material.TORCH) || e.getBlock().getType().equals(Material.WALL_TORCH) || e.getBlock().getType().equals(Material.REDSTONE_TORCH) || e.getBlock().getType().equals(Material.REDSTONE_WALL_TORCH))
				&& !e.getBlockReplacedState().getType().equals(Material.WATER) && !e.getBlockReplacedState().getType().equals(Material.LAVA)) {
			if (!playerpm(player.getName(), loc, 1)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在该空间放置火把的权限！"));
				e.setCancelled(true);
			}
		}
		else {
			if (!playerpm(player.getName(), loc, 0)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在该空间放置方块的权限！"));
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void place2(EntityChangeBlockEvent e) {
		NamespacedKey key = NamespacedKey.minecraft("jian_zhu_ji_qiao");
		if (e.getEntity().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
			FallingBlock block = (FallingBlock)e.getEntity();
			String playerName = block.getPersistentDataContainer().get(key, PersistentDataType.STRING);
			Location loc = e.getBlock().getLocation();
			if (!playerpm(playerName, loc, 0)) {
				e.setCancelled(true);
				loc.getWorld().dropItemNaturally(loc, new ItemStack(block.getBlockData().getMaterial()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void bucketPlace(PlayerBucketEmptyEvent e) {
		if (e.getBucket().equals(Material.MILK_BUCKET))
			return;
		Player player = e.getPlayer();
		Location loc = e.getBlockClicked().getRelative(e.getBlockFace()).getLocation();
		if (!playerpm(player.getName(), loc, 0)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在这个空间放置方块的权限！"));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void bucketFill(PlayerBucketFillEvent e) {
		if (e.getItemStack().getType().equals(Material.MILK_BUCKET))
			return;
		Player player = e.getPlayer();
		Block block = e.getBlockClicked().getRelative(e.getBlockFace());
		Location loc = block.getLocation();
		if (!playerpm(player.getName(), loc, 0)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有破坏该空间方块的权限！"));
			e.setCancelled(true);
			player.sendBlockChange(loc, block.getBlockData());
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void takeBook (PlayerTakeLecternBookEvent e) {
		Player player = e.getPlayer();
		Location loc = e.getLectern().getLocation();
		if (!playerpm(player.getName(), loc, 0)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有取走该书的权限！"));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void breakblock(BlockBreakEvent e) {
		Player player = e.getPlayer();
		Location loc = e.getBlock().getLocation();
		if (e.getBlock().getType().equals(Material.TORCH) || e.getBlock().getType().equals(Material.WALL_TORCH) || e.getBlock().getType().equals(Material.REDSTONE_TORCH) || e.getBlock().getType().equals(Material.REDSTONE_WALL_TORCH)) {
			if (!playerpm(player.getName(), loc, 1)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有破坏该空间火把的权限！"));
				e.setCancelled(true);
			}
		}
		else {
			if (!playerpm(player.getName(), loc, 0)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有破坏该空间方块的权限！"));
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void portalCreate(PortalCreateEvent e) {
		if (e.getReason().equals(PortalCreateEvent.CreateReason.NETHER_PAIR)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void interact2(PlayerInteractEvent e) {
		e.getPlayer().sendMessage("block: " + e.useInteractedBlock().name() + ", item: " + e.useItemInHand().name());
		if (!e.useInteractedBlock().equals(Event.Result.DENY) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if ((!e.getPlayer().isSneaking() || e.getItem() == null) && trigger(e.getClickedBlock(), e.getItem())) {
				PBlockInteractEvent event = new PBlockInteractEvent(e.getPlayer(), e.getClickedBlock());
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled())
					e.setUseInteractedBlock(Event.Result.DENY);
				return;
			}
		}
		if (e.getItem() != null && (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			PItemUseEvent itemUseEvent = new PItemUseEvent(e.getPlayer(), e.getItem());
			Bukkit.getPluginManager().callEvent(itemUseEvent);
			if (itemUseEvent.isCancelled())
				e.setUseItemInHand(Event.Result.DENY);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void interactBlock(PBlockInteractEvent e) {
		switch (e.getBlock().getType()) {
			case ENDER_CHEST:
			case CRAFTING_TABLE:
				return;
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
				Player player = e.getPlayer();
				Location loc = e.getBlock().getLocation();
				if (!playerpm(player.getName(), loc, 7)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有修改该空间牌子的权限！"));
					e.setCancelled(true);
				}
				return;
		}
		Player player = e.getPlayer();
		Location loc = e.getBlock().getLocation();
		if (!playerpm(player.getName(), loc, 2)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有使用该空间方块的权限！"));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void interact3(PlayerInteractEntityEvent e) {
		if ((e.getHand().equals(EquipmentSlot.HAND) || e.getHand().equals(EquipmentSlot.OFF_HAND)) && !(e.getRightClicked() instanceof Player)) {
			Player player = e.getPlayer();
			Location loc = e.getRightClicked().getLocation();
			if (e.getRightClicked() instanceof RideableMinecart || e.getRightClicked() instanceof Boat) {
				if (!playerpm(player.getName(), loc, 5)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有使用该空间交通工具的权限！"));
					e.setCancelled(true);
				}
			}
			else {
				if (!playerpm(player.getName(), loc, 4)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有使用该空间实体的权限！"));
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void interact4(EntityDamageByEntityEvent e) {
		Entity entity = e.getEntity();
		if ((entity instanceof Monster || entity instanceof Slime || entity instanceof Flying || entity instanceof Shulker || entity instanceof EnderDragon) && e.getEntity().getCustomName() == null)
			return;
		if (entity instanceof Projectile || entity instanceof Player)
			return;
		Player damager;
		boolean natural = false;
		if (e.getDamager() instanceof Player)
			damager = (Player)e.getDamager();
		else if (e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player) {
			damager = (Player) ((Projectile) e.getDamager()).getShooter();
		}
		else if (e.getDamager() instanceof Firework) {
			Firework firework = (Firework)e.getDamager();
			NamespacedKey namespacedKey = new NamespacedKey(PoorSpace.plugin, "firework");
			if (firework.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
				damager = Bukkit.getPlayerExact(firework.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING));
			}
			else {
				natural = true;
				damager = null;
			}
		}
		else {
			natural = true;
			damager = null;
		}
		if (natural) {
			if ((e.getEntity() instanceof ArmorStand || e.getEntity() instanceof Hanging)) {
				Location loc = e.getEntity().getLocation();
				NormalSpace space = SpaceManager.getSpace(loc);
				if (!space.canExplode())
					e.setCancelled(true);
			}
			return;
		}
		Location loc = e.getEntity().getLocation();
		if (!playerpm(damager.getName(), loc, 3)) {
			damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有攻击该空间实体的权限！"));
			e.setCancelled(true);
		}
		
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void shootFirework(EntityShootBowEvent e) {
		if (e.getProjectile() instanceof Firework && e.getEntity() instanceof Player) {
			Firework firework = (Firework)e.getProjectile();
			NamespacedKey namespacedKey = new NamespacedKey(PoorSpace.plugin, "firework");
			firework.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, e.getEntity().getName());
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void entity11 (ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow && e.getHitEntity() != null && e.getHitEntity() instanceof LivingEntity && !(e.getHitEntity() instanceof Player)) {
			if (e.getEntity().getShooter() instanceof Player) {
				Player player = (Player)e.getEntity().getShooter();
				Location loc = e.getHitEntity().getLocation();
				if (!playerpm(player.getName(), loc, 3)) {
					e.getEntity().setFireTicks(-1);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void interact5(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof ArmorStand && e.getHand().equals(EquipmentSlot.HAND) && !(e.getRightClicked() instanceof Player)) {
			Player player = e.getPlayer();
			Location loc = e.getRightClicked().getLocation();
			if (!playerpm(player.getName(), loc, 4)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有使用该空间实体的权限！"));
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void interact6 (HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player) {
			Player player = (Player)e.getRemover();
			Location loc = e.getEntity().getLocation();
			if (!playerpm(player.getName(), loc, 3)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有破坏该空间实体的权限！"));
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void entity7 (VehicleDamageEvent e) {
		if (e.getAttacker() instanceof Player) {

			if (e.getVehicle() instanceof RideableMinecart || e.getVehicle() instanceof Boat) {
				Player player = (Player) e.getAttacker();
				Location loc = e.getVehicle().getLocation();
				if (!playerpm(player.getName(), loc, 5)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有破坏该空间交通工具的权限！"));
					e.setCancelled(true);
				}
			}
			else if (e.getVehicle() instanceof Minecart) {
				Player player = (Player) e.getAttacker();
				Location loc = e.getVehicle().getLocation();
				if (!playerpm(player.getName(), loc, 3)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有破坏该空间实体的权限！"));
					e.setCancelled(true);
				}
			}

		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void entity8 (PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null) {

			if (cartArmorstand(e.getMaterial()) || spawnEggs(e.getMaterial())) {
				Player player = e.getPlayer();
				Location loc = e.getClickedBlock().getLocation();
				if (!playerpm(player.getName(), loc, 3)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在该空间内放置实体的权限！"));
					e.setCancelled(true);
				}
			}
			else if (transport(e.getMaterial())) {
				Player player = e.getPlayer();
				Location loc = e.getClickedBlock().getLocation();
				if (!playerpm(player.getName(), loc, 5)) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在该空间内放置交通工具的权限！"));
					e.setCancelled(true);
				}
			}

		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void entity9 (HangingPlaceEvent e) {
		Player player = e.getPlayer();
		Location loc = e.getEntity().getLocation();
		if (!playerpm(player.getName(), loc, 3)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有在该空间内放置实体的权限！"));
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void entity10 (PlayerFishEvent e) {
		if (e.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)) {
			Player player = e.getPlayer();
			Location loc = e.getCaught().getLocation();
			if (!playerpm(player.getName(), loc, 4)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("您没有使用该空间实体的权限！"));
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void explode(EntityExplodeEvent e) {
		List<Block> blockListCopy = new ArrayList<>();
	    blockListCopy.addAll(e.blockList());
		for (Block block : blockListCopy) {
			Location loc = block.getLocation();
			NormalSpace space = SpaceManager.getSpace(loc);
			if (!space.canExplode())
				e.blockList().remove(block);
		}
	}


	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void explode2 (EntityDamageEvent e) {
		if (!(e instanceof EntityDamageByEntityEvent) && (e.getEntity() instanceof ArmorStand || e.getEntity() instanceof Hanging)) {
			Location loc = e.getEntity().getLocation();
			NormalSpace space = SpaceManager.getSpace(loc);
			if (!space.canExplode())
				e.setCancelled(true);
		}
	}


	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void explode3 (HangingBreakEvent e) {
		if (e instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent)e).getRemover() instanceof Player)
			return;
		switch (e.getCause()) {
			case PHYSICS:
			case OBSTRUCTION:
				return;
		}
		Location loc = e.getEntity().getLocation();
		NormalSpace space = SpaceManager.getSpace(loc);
		if (!space.canExplode())
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void explode4(BlockExplodeEvent e) {
		List<Block> blockListCopy = new ArrayList<>();
		blockListCopy.addAll(e.blockList());
		for (Block block : blockListCopy) {
			Location loc = block.getLocation();
			NormalSpace space = SpaceManager.getSpace(loc);
			if (!space.canExplode())
				e.blockList().remove(block);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void explode5 (EntityChangeBlockEvent e) {
		if (e.getEntityType().equals(EntityType.RAVAGER)) {
			Location loc = e.getBlock().getLocation();
			NormalSpace space = SpaceManager.getSpace(loc);
			if (!space.canExplode())
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void mosterDamage(EntityChangeBlockEvent e) {
		if (monsters(e.getEntityType())) {
			Location loc = e.getBlock().getLocation();
			NormalSpace space = SpaceManager.getSpace(loc);
			if (!space.canExplode())
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void fire(BlockSpreadEvent e) {
		if (e.getSource().getType().equals(Material.FIRE)) {
			Location loc = e.getBlock().getLocation();
			NormalSpace space = SpaceManager.getSpace(loc);
			if (!space.canFire())
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void fire2(BlockBurnEvent e) {
		Location loc = e.getBlock().getLocation();
		NormalSpace space = SpaceManager.getSpace(loc);
		if (!space.canFire())
			e.setCancelled(true);
		
	}

	private boolean cartArmorstand(Material material) {
		switch(material) {
			case CHEST_MINECART:
			case COMMAND_BLOCK_MINECART:
			case FURNACE_MINECART:
			case HOPPER_MINECART:
			case TNT_MINECART:
			case ARMOR_STAND:
				return true;
			default:
				return false;
		}
	}

	private boolean transport(Material material) {
		switch (material) {
			case ACACIA_BOAT:
			case BIRCH_BOAT:
			case DARK_OAK_BOAT:
			case JUNGLE_BOAT:
			case OAK_BOAT:
			case SPRUCE_BOAT:
			case MINECART:
				return true;
			default:
				return false;
		}
	}
	
	private boolean spawnEggs(Material material) {
		if (material.name().endsWith("SPAWN_EGG"))
			return true;
		else
			return false;
	}
	
	private boolean monsters(EntityType type) {
		switch(type) {
		case WITHER:
		case ZOMBIE:
		case ZOMBIE_VILLAGER:
		case PIG_ZOMBIE:
		case HUSK:
			return true;
		default:
			return false;
		}
	}
	
	private boolean trigger(Block block, ItemStack item) {
		switch(block.getType()) {
			case CHEST:
			case ENDER_CHEST:
			case TRAPPED_CHEST:
			case WHITE_SHULKER_BOX:
			case ORANGE_SHULKER_BOX:
			case MAGENTA_SHULKER_BOX:
			case LIGHT_BLUE_SHULKER_BOX:
			case YELLOW_SHULKER_BOX:
			case LIME_SHULKER_BOX:
			case PINK_SHULKER_BOX:
			case GRAY_SHULKER_BOX:
			case LIGHT_GRAY_SHULKER_BOX:
			case CYAN_SHULKER_BOX:
			case PURPLE_SHULKER_BOX:
			case BLUE_SHULKER_BOX:
			case BROWN_SHULKER_BOX:
			case GREEN_SHULKER_BOX:
			case RED_SHULKER_BOX:
			case BLACK_SHULKER_BOX:
			case SHULKER_BOX:
			case ACACIA_BUTTON:
			case BIRCH_BUTTON:
			case DARK_OAK_BUTTON:
			case JUNGLE_BUTTON:
			case OAK_BUTTON:
			case SPRUCE_BUTTON:
			case STONE_BUTTON:
			case LEVER:
			case COMPARATOR:
			case REPEATER:
			case IRON_DOOR:
			case ACACIA_TRAPDOOR:
			case BIRCH_TRAPDOOR:
			case DARK_OAK_TRAPDOOR:
			case JUNGLE_TRAPDOOR:
			case OAK_TRAPDOOR:
			case SPRUCE_TRAPDOOR:
			case IRON_TRAPDOOR:
			case OAK_DOOR:
			case SPRUCE_DOOR:
			case BIRCH_DOOR:
			case JUNGLE_DOOR:
			case ACACIA_DOOR:
			case DARK_OAK_DOOR:
			case OAK_FENCE_GATE:
			case SPRUCE_FENCE_GATE:
			case BIRCH_FENCE_GATE:
			case JUNGLE_FENCE_GATE:
			case DARK_OAK_FENCE_GATE:
			case ACACIA_FENCE_GATE:
			case CRAFTING_TABLE:
			case FURNACE:
			case HOPPER:
			case BREWING_STAND:
			case ANVIL:
			case CHIPPED_ANVIL:
			case DAMAGED_ANVIL:
			case BEACON:
			case DISPENSER:
			case DROPPER:
			case NOTE_BLOCK:
			case ENCHANTING_TABLE:
			case DAYLIGHT_DETECTOR:
			case BLACK_BED:
			case BLUE_BED:
			case BROWN_BED:
			case CYAN_BED:
			case GRAY_BED:
			case GREEN_BED:
			case LIGHT_BLUE_BED:
			case LIGHT_GRAY_BED:
			case LIME_BED:
			case MAGENTA_BED:
			case ORANGE_BED:
			case PINK_BED:
			case PURPLE_BED:
			case RED_BED:
			case WHITE_BED:
			case YELLOW_BED:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
			case LOOM:
			case BARREL:
			case SMOKER:
			case BLAST_FURNACE:
			case CARTOGRAPHY_TABLE:
			case GRINDSTONE:
			case STONECUTTER:
			case BELL:
			case CAKE:
			case CAMPFIRE:
			case FLOWER_POT:
				return true;
			case COMPOSTER:
				return block.getBlockData().getAsString().contains("=8");
			case SWEET_BERRY_BUSH:
				String blockData = block.getBlockData().getAsString();
				return blockData.contains("=3") || (blockData.contains("=2") && (item == null || !item.getType().equals(Material.BONE_MEAL)));
			case JUKEBOX:
				return block.getBlockData().getAsString().contains("=true") || (item != null && item.getType().isRecord());
			case BEE_NEST:
			case BEEHIVE:
				return block.getBlockData().getAsString().contains("=5") &&
						(item != null && (item.getType().equals(Material.SHEARS) || item.getType().equals(Material.GLASS_BOTTLE)));
			default:
				return false;
		}
	}
	
	public static boolean playerpm(String player, Location loc, int pmid) {

		NormalSpace space = SpaceManager.getSpace(loc);
		if (space.owner() != null && player.equals(space.owner()))
			return true;
		int group = checkGroup(space, player);
		char[] pm = space.permission(group);
		if (pm[pmid] == '1')
			return true;
		else
			return false;

	}
	
	private static int checkGroup(NormalSpace space, String player) {
		for (int i = 1; i < 4; i++) {
			for (String s : space.group(i)) {
				if (s.startsWith(">")) {
					SpaceGroup group = new SpaceGroup(s.substring(1));
					if (group.exists() && group.contains(player))
						return i;
				}
				else if (s.equals(player))
					return i;
			}
		}
		return 4;
	}
	
	
//	private void sendActionBarMessage(Player player, String message) {
//        IChatBaseComponent dummyComponent = ChatSerializer.a("{\"text\":\""+message+"\"}");
//        PacketPlayOutChat packet = new PacketPlayOutChat(dummyComponent, ChatMessageType.GAME_INFO);
//        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
//	}

}
