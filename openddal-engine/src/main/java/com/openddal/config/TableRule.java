package com.openddal.config;

import java.io.Serializable;

import com.openddal.route.rule.ObjectNode;

/**
 * @author <a href="mailto:jorgie.mail@gmail.com">jorgie li</a>
 */
public class TableRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private ObjectNode metadataNode;
    private TableRuleGroup ownerGroup;


    public TableRule(String name) {
        this.name = name;
    }

    public TableRule(String name, ObjectNode metadataNode) {
        super();
        this.name = name;
        this.metadataNode = metadataNode;
    }

    public ObjectNode getMetadataNode() {
        return metadataNode;
    }
    
    public ObjectNode randomMetadataNodeIfNeeded() {
        return getMetadataNode();
    } 

    public String getName() {
        return name;
    }

    public void setMetadataNode(ObjectNode metadataNode) {
        this.metadataNode = metadataNode;
    }

    public static TableRule newFixedNodeTable(String name, ObjectNode metadataNode) {
        return new TableRule(name, metadataNode);
    }

    public static TableRule newFixedNodeIndex(String name, ObjectNode metadataNode) {
        return new TableRule(name, metadataNode);
    }
    
    public static MultiNodeTableRule newMultiNodeTable(String name, ObjectNode metadataNode, ObjectNode[] objectNodes) {
        return new MultiNodeTableRule(name, metadataNode, objectNodes);
    }
    
    public static MultiNodeTableRule newMultiNodeIndex(String name, ObjectNode metadataNode, ObjectNode[] objectNodes) {
        return new MultiNodeTableRule(name, metadataNode,objectNodes);
    }
    
    public static ShardedTableRule newShardedTable(String name, ObjectNode metadataNode, ObjectNode[] objectNodes) {
        return new ShardedTableRule(name, metadataNode, objectNodes);
    }

    public TableRuleGroup getOwnerGroup() {
        return ownerGroup;
    }

    protected void setOwnerGroup(TableRuleGroup ownerGroup) {
        this.ownerGroup = ownerGroup;
    }
    
    

}
