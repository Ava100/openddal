package com.openddal.engine;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.openddal.config.Configuration;
import com.openddal.config.DataSourceProvider;
import com.openddal.config.GlobalTableRule;
import com.openddal.config.SequnceConfig;
import com.openddal.config.Shard;
import com.openddal.config.Shard.ShardItem;
import com.openddal.config.ShardedTableRule;
import com.openddal.config.TableRule;
import com.openddal.config.TableRuleGroup;
import com.openddal.config.parser.XmlConfigParser;
import com.openddal.route.rule.ObjectNode;
import com.openddal.util.StringUtils;
import com.openddal.util.Utils;

public final class SessionFactoryBuilder {

    private Configuration configuration = new Configuration();

    public static SessionFactoryBuilder newBuilder() {
        return new SessionFactoryBuilder();
    }

    public SessionFactoryBuilder fromXml(String fromXml) {

        if (StringUtils.isNullOrEmpty(fromXml)) {
            throw new IllegalArgumentException("xml config file must not be null");
        }
        InputStream source = null;
        try {
            source = Utils.getResourceAsStream(fromXml);
            if (source == null) {
                throw new IllegalArgumentException("Can't load config file " + fromXml + " from classpath.");
            }
            XmlConfigParser parser = new XmlConfigParser(source);
            parser.parse(configuration);
        } finally {
            try {
                source.close();
            } catch (Exception e) {
                // ignored
            }
        }

        return this;
    }

    public SessionFactoryBuilder applySettings(Properties settings) {
        if (settings == null) {
            throw new IllegalArgumentException();
        }
        configuration.settings = settings;
        return this;
    }

    public SessionFactoryBuilder dbCluster(List<Shard> cluster) {
        vaildateCluster(cluster);
        configuration.cluster = cluster;
        return this;
    }

    public SessionFactoryBuilder dataSourceProvider(DataSourceProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException();
        }
        configuration.provider = provider;
        return this;
    }

    public SessionFactoryBuilder addTableRuleGrop(TableRuleGroup group) {
        validateTableRule(group);
        List<ShardedTableRule> tableRules = group.getTableRules();
        configuration.tableRules.addAll(tableRules);
        return this;
    }

    public SessionFactoryBuilder addTableRule(ShardedTableRule table) {
        validateTableRule(table);
        configuration.tableRules.add(table);
        return this;
    }

    public SessionFactoryBuilder addTableRule(GlobalTableRule table) {
        validateTableRule(table);
        configuration.tableRules.add(table);
        return this;
    }

    public SessionFactoryBuilder addTableRule(TableRule table) {
        validateTableRule(table);
        configuration.tableRules.add(table);
        return this;
    }

    public SessionFactoryBuilder addSequnce(SequnceConfig sequnce) {
        validateTableRule(sequnce);
        configuration.sequnces.add(sequnce);
        return this;
    }

    public SessionFactoryBuilder defaultShardName(String shardName) {
        configuration.defaultShardName = shardName;
        return this;
    }

    public SessionFactoryBuilder forceLoadTableMate(boolean force) {
        configuration.forceLoadTableMate = force;
        return this;
    }

    public SessionFactory build() {
        beforeBuild();
        System.getProperties().putAll(configuration.settings);
        Database database = new Database(configuration);
        Engine engine = new Engine(database);
        return engine;
    }

    private void validateTableRule(TableRule object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }
        if (StringUtils.isNullOrEmpty(object.getName())) {
            throw new IllegalArgumentException();
        }
        switch (object.getType()) {
        case TableRule.SHARDED_NODE_TABLE:
            ShardedTableRule shardingTable = (ShardedTableRule) object;
            List<String> ruleColumns = shardingTable.getRuleColumns();
            if (ruleColumns == null || ruleColumns.isEmpty()) {
                throw new IllegalArgumentException();
            }
            for (String string : ruleColumns) {
                if (StringUtils.isNullOrEmpty(string)) {
                    throw new IllegalArgumentException();
                }
            }
            if (shardingTable.getPartitioner() == null) {
                throw new IllegalArgumentException();
            }
            ObjectNode[] objectNodes = shardingTable.getObjectNodes();
            if (objectNodes == null) {
                throw new IllegalArgumentException();
            }
            validateTableNode(object, objectNodes);
            break;
        case TableRule.GLOBAL_NODE_TABLE:
            GlobalTableRule multiNodeObject = (GlobalTableRule) object;
            objectNodes = multiNodeObject.getBroadcasts();
            if (objectNodes == null) {
                throw new IllegalArgumentException();
            }
            validateTableNode(object, objectNodes);
            break;
        case TableRule.FIXED_NODE_TABLE:
            if (object.getMetadataNode() == null) {
                throw new IllegalArgumentException();
            }
            validateTableNode(object, object.getMetadataNode());
            break;

        default:
            throw new IllegalArgumentException("invalid table type");
        }

    }

    private void validateTableNode(TableRule object, ObjectNode... nodes) {
        for (ObjectNode objectNode : nodes) {
            String shardName = objectNode.getShardName();
            boolean matched = false;
            for (Shard shard : configuration.cluster) {
                if (StringUtils.equals(shardName, shard.getName())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new IllegalArgumentException(
                        "table " + object.getName() + " has invalid node " + objectNode.toString());
            }
        }
    }

    private void validateTableRule(TableRuleGroup tableGroup) {
        List<ShardedTableRule> tables = tableGroup.getTableRules();
        if (tables == null || tables.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int columns = -1;
        for (ShardedTableRule tableRule : tables) {
            if (tableRule == null) {
                throw new IllegalArgumentException();
            }
            if (StringUtils.isNullOrEmpty(tableRule.getName())) {
                throw new IllegalArgumentException();
            }
            ShardedTableRule shardedTableRule = (ShardedTableRule) tableRule;
            List<String> ruleColumns = shardedTableRule.getRuleColumns();
            if (ruleColumns == null || ruleColumns.isEmpty()) {
                throw new IllegalArgumentException("The TableRule columns is required if use table rule columns");
            }
            if (columns == -1) {
                columns = ruleColumns.size();
            } else if (columns != ruleColumns.size()) {
                throw new IllegalArgumentException("The length of TableRule columns must be equal");
            }

        }

    }

    private void vaildateCluster(List<Shard> cluster) {
        if (cluster == null || cluster.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (Shard item : cluster) {
            if (StringUtils.isNullOrEmpty(item.getName())) {
                throw new IllegalArgumentException("Shard's name required.");
            }
            List<ShardItem> shardItems = item.getShardItems();
            for (ShardItem shardItem : shardItems) {
                if (StringUtils.isNullOrEmpty(shardItem.getRef())) {
                    throw new IllegalArgumentException("member 's ref is required.");
                }
                if (shardItem.getwWeight() <= 0 && shardItem.getrWeight() <= 0) {
                    throw new IllegalArgumentException("member 's weight not be less than zero.");
                }
                if (shardItem.getwWeight() <= 0) {
                    shardItem.setReadOnly(true);
                }
            }
        }
    }

    private void beforeBuild() {
        vaildateCluster(configuration.cluster);
        for (TableRule table : configuration.tableRules) {
            validateTableRule(table);
        }
        if (configuration.provider == null) {
            throw new IllegalArgumentException("DataSourceProvider is required.");
        }
    }

}
