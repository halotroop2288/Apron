package modloadermp;

import java.util.List;
import modloader.BaseMod;
import modloader.ModLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.OpenContainerS2CPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ModLoaderMp {
	public static final String NAME = "ModLoaderMP";
	public static final String VERSION = "Babric 1.7.3";
	private static boolean hasInit = false;
	private static boolean packet230Received = false;
	private static Map<Integer, NetClientHandlerEntity> netClientHandlerEntityMap = new HashMap<>();
	private static Map<Integer, BaseModMp> guiModMap = new HashMap<>();
	
	public static void Init() {
		if (!ModLoaderMp.hasInit) {
			init();
		}
	}
	
	public static void HandleAllPackets(final Packet230ModLoader packet) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		ModLoaderMp.packet230Received = true;
		if (packet.modId == NAME.hashCode()) {
			switch (packet.packetType) {
				case 0: {
					handleModCheck(packet);
					break;
				}
				case 1: {
					handleTileEntityPacket(packet);
					break;
				}
			}
		} else if (packet.modId == "Spawn".hashCode()) {
			NetClientHandlerEntity netclienthandlerentity = HandleNetClientHandlerEntities(packet.packetType);
			if (netclienthandlerentity != null && ISpawnable.class.isAssignableFrom(netclienthandlerentity.entityClass)) {
				try {
					Entity entity = netclienthandlerentity.entityClass.getConstructor(World.class).newInstance(ModLoader.getMinecraftInstance().world);
					((ISpawnable)entity).spawn(packet);
					((ClientWorld)ModLoader.getMinecraftInstance().world).method_1495(entity.entityId, entity);
				} catch (Exception var4) {
					ModLoader.getLogger().throwing("ModLoader", "handleCustomSpawn", var4);
					ModLoader.ThrowException(String.format("Error initializing entity of type %s.", packet.packetType), var4);
				}
			}
		} else {
			for(int i = 0; i < ModLoader.getLoadedMods().size(); ++i) {
				BaseMod basemod = ModLoader.getLoadedMods().get(i);
				if (basemod instanceof BaseModMp) {
					BaseModMp basemodmp = (BaseModMp)basemod;
					if (basemodmp.getId() == packet.modId) {
						basemodmp.HandlePacket(packet);
						break;
					}
				}
			}
		}
	}
	
	public static NetClientHandlerEntity HandleNetClientHandlerEntities(final int aInteger1) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (ModLoaderMp.netClientHandlerEntityMap.containsKey(aInteger1)) {
			return ModLoaderMp.netClientHandlerEntityMap.get(aInteger1);
		}
		return null;
	}

	public static void SendPacket(BaseModMp basemodmp, Packet230ModLoader packet) {
		if (!hasInit) {
			init();
		}

		if (basemodmp == null) {
			IllegalArgumentException illegalargumentexception = new IllegalArgumentException("baseModMp cannot be null.");
			ModLoader.getLogger().throwing("ModLoaderMp", "SendPacket", illegalargumentexception);
			ModLoader.ThrowException("baseModMp cannot be null.", illegalargumentexception);
		} else {
			packet.modId = basemodmp.getId();
			sendPacket(packet);
		}
	}
	
	public static void RegisterGUI(final BaseModMp basemodmp, final int i) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (ModLoaderMp.guiModMap.containsKey(i)) {
			Log("RegisterGUI error: inventoryType already registered.");
		}
		else {
			ModLoaderMp.guiModMap.put(i, basemodmp);
		}
	}
	
	public static void HandleGUI(final OpenContainerS2CPacket packet) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		final BaseModMp basemodmp = ModLoaderMp.guiModMap.get(packet.inventoryType);
		final Screen guiScreen = basemodmp.HandleGUI(packet.inventoryType);
		if (guiScreen != null) {
			ModLoader.OpenGUI(ModLoader.getMinecraftInstance().player, guiScreen);
			ModLoader.getMinecraftInstance().player.container.currentContainerId = packet.containerId;
		}
	}

	public static void RegisterNetClientHandlerEntity(Class<? extends Entity> class1, int i) {
		RegisterNetClientHandlerEntity(class1, false, i);
	}

	public static void RegisterNetClientHandlerEntity(Class<? extends Entity> class1, boolean flag, int i) {
		if (!hasInit) {
			init();
		}

		if (i > 255) {
			Log("RegisterNetClientHandlerEntity error: entityId cannot be greater than 255.");
		} else if (netClientHandlerEntityMap.containsKey(i)) {
			Log("RegisterNetClientHandlerEntity error: entityId already registered.");
		} else {
			if (i > 127) {
				i -= 256;
			}

			netClientHandlerEntityMap.put(i, new NetClientHandlerEntity(class1, flag));
		}
	}
	
	public static void SendKey(final BaseModMp basemodmp, final int i) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (basemodmp == null) {
			final IllegalArgumentException e = new IllegalArgumentException("baseModMp cannot be null.");
			ModLoader.getLogger().throwing(NAME, "SendKey", e);
			ModLoader.ThrowException("baseModMp cannot be null.", e);
		}
		else {
			final Packet230ModLoader packet230modloader = new Packet230ModLoader();
			packet230modloader.modId = NAME.hashCode();
			packet230modloader.packetType = 1;
			packet230modloader.dataInt = new int[] {basemodmp.getId(), i};
			sendPacket(packet230modloader);
		}
	}
	
	public static void Log(final String s) {
		ModLoader.getLogger().fine(s);
	}

	private static void init() {
		hasInit = true;
		AbstractPacket.register(230, true, true, Packet230ModLoader.class);
		Log("ModLoaderMP Beta 1.7.3 unofficial Initialized");
	}

	private static void handleModCheck(Packet230ModLoader originalPacket) {
		Packet230ModLoader newPacket = new Packet230ModLoader();
		newPacket.modId = "ModLoaderMP".hashCode();
		newPacket.packetType = 0;
		newPacket.dataString = new String[ModLoader.getLoadedMods().size()];

		for(int i = 0; i < ModLoader.getLoadedMods().size(); ++i) {
			newPacket.dataString[i] = ModLoader.getLoadedMods().get(i).toString();
		}

		sendPacket(newPacket);
	}

	private static void handleTileEntityPacket(Packet230ModLoader packet) {
		if (packet.dataInt != null && packet.dataInt.length >= 5) {
			int i = packet.dataInt[0];
			int j = packet.dataInt[1];
			int k = packet.dataInt[2];
			int l = packet.dataInt[3];
			int i1 = packet.dataInt[4];
			int[] ai = new int[packet.dataInt.length - 5];
			System.arraycopy(packet.dataInt, 5, ai, 0, packet.dataInt.length - 5);
			float[] af = packet.dataFloat;
			String[] as = packet.dataString;

			for(int j1 = 0; j1 < ModLoader.getLoadedMods().size(); ++j1) {
				BaseMod basemod = ModLoader.getLoadedMods().get(j1);
				if (basemod instanceof BaseModMp) {
					BaseModMp basemodmp = (BaseModMp)basemod;
					if (basemodmp.getId() == i) {
						basemodmp.HandleTileEntityPacket(j, k, l, i1, ai, af, as);
						break;
					}
				}
			}
		} else {
			Log("Bad TileEntityPacket received.");
		}
	}

	private static void sendPacket(Packet230ModLoader packet) {
		if (packet230Received && ModLoader.getMinecraftInstance().world != null && ModLoader.getMinecraftInstance().world.isClient) {
			ModLoader.getMinecraftInstance().getPacketHandler().sendPacket(packet);
		}
	}
	
	public static BaseModMp GetModInstance(final Class<? extends BaseModMp> v1) {
		for (BaseMod basemod : ModLoader.getLoadedMods()) {
			if (basemod instanceof BaseModMp) {
				final BaseModMp basemodmp = (BaseModMp) basemod;
				if (v1.isInstance(basemodmp)) {
					return basemodmp;
				}
			}
		}
		return null;
	}

	private ModLoaderMp() {}
}
