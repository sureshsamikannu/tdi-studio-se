package org.talend.designer.dbmap.language.generation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.mysql.MysqlGenerationManager;
import org.talend.designer.dbmap.language.oracle.OracleGenerationManager;

public class DbGenerationManagerTest {

    private DbMapComponent dbMapComponent;

    private DbGenerationManager dbManager;

    private ExternalDbMapEntry extMapEntry;

    private List<ExternalDbMapEntry> tableEntries;

    @Before
    public void setUp() throws Exception {
        dbMapComponent = new DbMapComponent();

        ExternalDbMapData externalDbMapData = new ExternalDbMapData();
        dbMapComponent.setExternalData(externalDbMapData);

        List<ExternalDbMapTable> inputTables = new ArrayList<>();
        externalDbMapData.setInputTables(inputTables);

        ExternalDbMapTable externalDbMapTable = new ExternalDbMapTable();
        inputTables.add(externalDbMapTable);
        externalDbMapTable.setName("t1");
        externalDbMapTable.setAlias("t1");
        externalDbMapTable.setTableName("t1");
        externalDbMapTable.setJoinType("NO_JOIN");

        tableEntries = new ArrayList<>();
        externalDbMapTable.setMetadataTableEntries(tableEntries);
        extMapEntry = new ExternalDbMapEntry("id", "t1.id");
        tableEntries.add(extMapEntry);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] columns = new String[] { "\\\"id\\\"" };
        incomingConnections.add(createConnection("t1", "t1", "id", columns));
        dbMapComponent.setIncomingConnections(incomingConnections);

        dbManager = new GenericDbGenerationManager();

    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        dbManager = null;
        extMapEntry = null;
        tableEntries = null;
    }

    @Test
    public void testInitExpression() {
        checkValue("t1.\\\"id\\\"");
    }

    public void checkValue(String expression) {
        Assert.assertEquals(expression, dbManager.initExpression(dbMapComponent, extMapEntry));
    }

    private IConnection createConnection(String schemaName, String tableName, String label, String[] columns) {
        Connection connection = mock(Connection.class);
        when(connection.getName()).thenReturn(tableName);
        IMetadataTable metadataTable = new MetadataTable();
        metadataTable.setLabel(tableName);
        metadataTable.setTableName(tableName);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : columns) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(label);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        metadataTable.setListColumns(listColumns);
        when(connection.getMetadataTable()).thenReturn(metadataTable);

        return connection;
    }

    @Test
    public void testBuildSqlSelectForGlobalMap() {
        String schema = "((String)globalMap.get(\"schema\"))";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";

        // ((String)globalMap.get("tableName")).columnName
        init("", main_table, lookup_table);
        String expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"main_table\"))+ \".id, \" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " \" +((String)globalMap.get(\"main_table\"))+ \".age, \" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n" + " \" +((String)globalMap.get(\"main_table\"))+ \" , \" +((String)globalMap.get(\"lookup_table\"))";
        MysqlGenerationManager manager = new MysqlGenerationManager();
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        OracleGenerationManager oManager = new OracleGenerationManager();
        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // schema.((String)globalMap.get("tableName")).columnName
        init(schema, main_table, lookup_table);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".id, \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".age, \""
                + " +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \" , \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // schema.((String)globalMap.get("tableName")).columnName
        schema = "my_schema";
        init(schema, main_table, lookup_table);
        expectedQuery = "\"SELECT\n"
                + "my_schema.\" +((String)globalMap.get(\"main_table\"))+ \".id, my_schema.\" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " my_schema.\" +((String)globalMap.get(\"main_table\"))+ \".age,"
                + " my_schema.\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " my_schema.\" +((String)globalMap.get(\"main_table\"))+ \" , my_schema.\" +((String)globalMap.get(\"lookup_table\"))";
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // ((String)globalMap.get("schema")).tableName.columnName
        schema = "((String)globalMap.get(\"schema\"))";
        main_table = "main_table";
        init(schema, main_table, lookup_table);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"schema\"))+ \".main_table.id, \" +((String)globalMap.get(\"schema\"))+ \".main_table.name,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".main_table.age, \""
                + " +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"schema\"))+ \".main_table , \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

    }

    private void init(String schema, String main_table, String lookup_table) {
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "name", "age" };
        String[] lookupEndtities = new String[] { "id", "score" };
        incomingConnections.add(mockConnection(schema, main_table, mainTableEntities));
        incomingConnections.add(mockConnection(schema, lookup_table, lookupEndtities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        String mainTableName = "".equals(schema) ? main_table : schema + "." + main_table;
        inputTable.setTableName(mainTableName);
        inputTable.setName(mainTableName);
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup table
        inputTable = new ExternalDbMapTable();
        String lookupName = "".equals(schema) ? lookup_table : schema + "." + lookup_table;
        inputTable.setTableName(lookupName);
        inputTable.setName(lookupName);
        entities = getMetadataEntities(lookupEndtities, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName("grade");
        String[] names = new String[] { "id", "name", "age", "score" };
        String[] expressions = new String[] { mainTableName + ".id", mainTableName + ".name", mainTableName + ".age",
                lookupName + ".score" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("grade");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);
    }

    private IConnection mockConnection(String schemaName, String tableName, String[] columns) {
        Connection connection = mock(Connection.class);
        Node node = mock(Node.class);
        ElementParameter param = new ElementParameter(node);
        param.setName("ELT_SCHEMA_NAME");
        param.setValue(schemaName);
        when(node.getElementParameter("ELT_SCHEMA_NAME")).thenReturn(param);
        param = new ElementParameter(node);
        param.setName("ELT_TABLE_NAME");
        param.setValue(tableName);
        when(node.getElementParameter("ELT_TABLE_NAME")).thenReturn(param);
        String tName = "".equals(schemaName) ? tableName : schemaName + "." + tableName;
        when(connection.getName()).thenReturn(tName);
        when(connection.getSource()).thenReturn(node);
        IMetadataTable table = new MetadataTable();
        table.setLabel(tableName);
        table.setTableName(tableName);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : columns) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        when(connection.getMetadataTable()).thenReturn(table);

        return connection;
    }

    private List<ExternalDbMapEntry> getMetadataEntities(String[] entitiesName, String[] expressions) {
        List<ExternalDbMapEntry> entities = new ArrayList<ExternalDbMapEntry>();
        for (int i = 0; i < entitiesName.length; i++) {
            ExternalDbMapEntry entity = new ExternalDbMapEntry();
            entity.setName(entitiesName[i]);
            if (i < expressions.length && !"".equals(expressions[i]) && expressions[i] != null) {
                entity.setExpression(expressions[i]);
            }
            entities.add(entity);
        }
        return entities;
    }

    private MetadataTable getMetadataTable(String[] entitiesName) {
        MetadataTable table = new MetadataTable();
        for (String element : entitiesName) {
            MetadataColumn column = new MetadataColumn();
            column.setLabel(element);
            table.getListColumns().add(column);
        }
        return table;
    }

}
