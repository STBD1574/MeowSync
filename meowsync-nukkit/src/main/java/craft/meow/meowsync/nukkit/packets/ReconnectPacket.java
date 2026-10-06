package craft.meow.meowsync.nukkit.packets;

import cn.nukkit.network.protocol.DataPacket;
import lombok.ToString;

/**
 * 请求代理端让玩家重新连接 (s2p)。
 *
 * <p>服务端把该数据包发给代理端，代理端随后让客户端重新进入。
 * 该数据包没有负载。
 */
@ToString
public class ReconnectPacket extends DataPacket {

    public static final int NETWORK_ID = 503;

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
        // 无负载
    }

    @Override
    public void decode() {
        // 无负载
    }
}
