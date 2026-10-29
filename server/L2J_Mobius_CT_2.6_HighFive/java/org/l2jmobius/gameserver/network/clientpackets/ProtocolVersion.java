/*
 * Copyright (c) 2013 L2jMobius
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.config.ServerConfig;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketShuffler;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.network.SalvationProtocol;
import org.l2jmobius.gameserver.network.serverpackets.KeyPacket;

/**
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public class ProtocolVersion extends ClientPacket
{
	private static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	private int _version;
	
	@Override
	protected void readImpl()
	{
		try
		{
			_version = readInt();
			
			// Salvation protocol 140 may append a 0x104-byte handshake block
			// (0x100-byte data field followed by a 4-byte check field).
			// Consume the complete optional block so it cannot be mistaken for
			// another packet. This does not by itself enable Salvation support.
			if ((_version == 140) && (remaining() == 0x104))
			{
				readBytes(0x104);
			}
		}
		catch (Exception e)
		{
			_version = 0;
		}
	}
	
	@Override
	protected void runImpl()
	{
		// This packet is never encrypted.
		final GameClient client = getClient();
		if (_version == -2)
		{
			// This is just a ping attempt from the new C2 client.
			client.disconnect();
		}
		else if (SalvationProtocol.isEnabledFor(_version))
		{
			// Salvation client handshake. The adapter consumes the extra handshake
			// block in readImpl and registers the per-client opcode shuffler when
			// SalvationShuffle is enabled.
			client.setProtocolVersion(_version);
			client.setProtocolOk(true);
			client.setSalvationMode(true);
			
			int obfuscationKey = 0;
			if (ServerConfig.SALVATION_SHUFFLE)
			{
				final PacketShuffler shuffler = new PacketShuffler();
				int seed = 0;
				while (seed == 0)
				{
					seed = Rnd.get(Integer.MIN_VALUE, Integer.MAX_VALUE);
				}
				
				shuffler.makeShuffledPacket(seed);
				obfuscationKey = seed;
				client.setPacketShuffler(shuffler);
				LOGGER_ACCOUNTING.info("Salvation client " + client + ": opcode shuffling enabled (seed " + seed + ")");
			}
			
			client.sendPacket(new KeyPacket(client.enableCrypt(), 1, obfuscationKey));
			LOGGER_ACCOUNTING.info("SALVATION: protocol 140 client accepted, KeyPacket sent (isClassic=" + (ServerConfig.SALVATION_IS_CLASSIC || ((ServerConfig.SERVER_LIST_TYPE & 0x400) == 0x400)) + ")");
		}
		else if (!ServerConfig.PROTOCOL_LIST.contains(_version))
		{
			LOGGER_ACCOUNTING.warning("Wrong protocol version " + _version + ", " + client);
			client.setProtocolOk(false);
			client.close(new KeyPacket(client.enableCrypt(), 0));
		}
		else
		{
			client.setProtocolVersion(_version);
			client.setProtocolOk(true);
			client.sendPacket(new KeyPacket(client.enableCrypt(), 1));
		}
	}
}
