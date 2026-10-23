package tuhljin.automagy.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.lib.TjUtil;

public abstract class TemplateMessageItemStacksList<T extends TemplateMessageItemStacksList> extends BlockTiedMessageToClient<T> {
    protected ArrayList<ItemStack> list = null;
    protected boolean partial;

    public TemplateMessageItemStacksList() {
        this.partial = false;
    }

    public TemplateMessageItemStacksList(int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial) {
        super(dim, x, y, z);
        this.list = list;
        this.partial = partial;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        int size = buf.readInt();
        this.list = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            this.list.add(TjUtil.itemStackFromBytes(buf));
        }

        this.partial = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        int size = this.list == null ? 0 : this.list.size();
        buf.writeInt(size);

        for (int i = 0; i < size; i++) {
            TjUtil.itemStackToBytes(buf, this.list.get(i));
        }

        buf.writeBoolean(this.partial);
    }
}
