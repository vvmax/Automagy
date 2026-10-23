package tuhljin.automagy.tiles;

public interface IInventariumMemoryUser {
    void breakLink();

    void setLink(TileEntityInventarium var1);

    boolean isLinkedTo(TileEntityInventarium var1);
}
