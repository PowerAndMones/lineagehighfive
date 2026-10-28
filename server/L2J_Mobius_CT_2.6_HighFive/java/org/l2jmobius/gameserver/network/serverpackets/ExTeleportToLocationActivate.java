package org.l2jmobius.gameserver.network.serverpackets;
	
import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.entity.actor.Creature;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;
	
/**
 * Sent when a teleport finishes so the client leaves the fading state.
 * Ported from the Mobius 5.0 Salvation reference.
 */
public class ExTeleportToLocationActivate extends ServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	
	public ExTeleportToLocationActivate(Creature creature)
	{
		_objectId = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		_heading = creature.getHeading();
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_TELEPORT_TO_LOCATION_ACTIVATE.writeId(this, buffer);
		buffer.writeInt(_objectId);
		buffer.writeInt(_x);
		buffer.writeInt(_y);
		buffer.writeInt(_z);
		buffer.writeInt(0); // Unknown (this isn't instanceId)
		buffer.writeInt(_heading);
		buffer.writeInt(0); // Unknown
	}
}
