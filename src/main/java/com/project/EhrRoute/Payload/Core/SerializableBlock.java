package com.project.EhrRoute.Payload.Core;

public class SerializableBlock
{
    private SerializableBlockHeader blockHeader;
    private SerializableTransaction transaction;

    public SerializableBlock() { }
    public SerializableBlock(SerializableBlockHeader blockHeader, SerializableTransaction transaction) {
        this.blockHeader = blockHeader;
        this.transaction = transaction;
    }

    public SerializableBlockHeader getBlockHeader() {
        return blockHeader;
    }
    public SerializableTransaction getTransaction() {
        return transaction;
    }
    public void setBlockHeader(SerializableBlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }
    public void setTransaction(SerializableTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return "SerializableBlock { " +
                "blockHeader:" + blockHeader +
                ", transaction:" + transaction +
                " }";
    }
}
