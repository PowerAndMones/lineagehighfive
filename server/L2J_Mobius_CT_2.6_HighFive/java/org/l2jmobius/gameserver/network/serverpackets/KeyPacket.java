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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.config.ServerConfig;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SalvationProtocol;
import org.l2jmobius.gameserver.network.ServerPackets;

public class KeyPacket extends ServerPacket
{
	private final byte[] _key;
	private final int _result;
	private final int _obfuscationKey;
	
	public KeyPacket(byte[] key, int result)
	{
		_key = key;
		_result = result;
		_obfuscationKey = 0;
	}
	
	/**
	 * KeyPacket with an opcode obfuscation seed for Salvation clients.
	 * @param key the Blowfish session key
	 * @param result protocol check result (0 - wrong protocol, 1 - ok)
	 * @param obfuscationKey the non-zero opcode shuffling seed, or 0 to disable shuffling
	 */
	public KeyPacket(byte[] key, int result, int obfuscationKey)
	{
		_key = key;
		_result = result;
		_obfuscationKey = obfuscationKey;
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.VERSION_CHECK.writeId(this, buffer);
		buffer.writeByte(_result); // 0 - wrong protocol, 1 - protocol ok
		for (int i = 0; i < 8; i++)
		{
			buffer.writeByte(_key[i]); // key
		}
		
		buffer.writeInt(ServerConfig.PACKET_ENCRYPTION); // use blowfish encryption
		buffer.writeInt(ServerConfig.SERVER_ID); // server id
		buffer.writeByte(1);
		buffer.writeInt(_obfuscationKey); // obfuscation key (non-zero enables client opcode shuffling)
		
		// Salvation clients read one extra byte after the obfuscation key.
		// It tells the client which data set to load: classic (lighter) or live
		// (heavy, includes the item upgrade system). Classic mode avoids the
		// 32-bit "Insufficient Memory" crash on character selection.
		if ((client != null) && (client.getProtocolVersion() == SalvationProtocol.SALVATION_PROTOCOL_REVISION))
		{
			buffer.writeByte(ServerConfig.SALVATION_IS_CLASSIC || ((ServerConfig.SERVER_LIST_TYPE & 0x400) == 0x400)); // isClassic
		}
	}
}
