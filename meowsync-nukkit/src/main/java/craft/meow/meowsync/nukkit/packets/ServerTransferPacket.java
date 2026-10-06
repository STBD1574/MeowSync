package craft.meow.meowsync.nukkit.packets;

import cn.nukkit.network.protocol.DataPacket;
import lombok.Getter;
import lombok.ToString;

/**
 * 将玩家转移至指定服务器 (s2p)。
 *
 * <p>服务端把该数据包发给代理端，代理端随后执行玩家转移。
 */
@ToString
public class ServerTransferPacket extends DataPacket {

    public static final int NETWORK_ID = 502;

    @Getter
    private String serverName = "";

    public ServerTransferPacket() {
    }

    public ServerTransferPacket(String serverName) {
        this.serverName = serverName == null ? "" : serverName;
    }

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public byte pid() {
        return (byte) NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.serverName == null ? "" : this.serverName);
    }

    @Override
    public void decode() {
        this.serverName = this.getString();
    }
}
